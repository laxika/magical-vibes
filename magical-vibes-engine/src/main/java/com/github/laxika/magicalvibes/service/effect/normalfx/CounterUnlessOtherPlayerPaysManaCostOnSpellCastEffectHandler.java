package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Ice Cave's "any other player may pay that spell's mana cost. If a player does, counter the spell": the triggering
 * spell's mana cost with its chosen X, read as the ability resolves from the live spell, else the stack entry's
 * triggering spell snapshot, is offered to every player but the caster: the handler inserts the payment as the
 * ability's next effect, a {@link MayPayManaEffect} with {@link MayPayPayer#ANY_OTHER_PLAYER} and
 * {@link CounterSpellEffect}, which resolves (and resumes after the players answer) as any "you may pay" does.
 */
@Component
public class CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry spell = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(entry.getTriggeringCardId()))
                .findFirst()
                .orElse(entry.getTriggeringSpellSnapshot());
        if (spell == null || spell.getCard().getManaCost() == null) {
            return;
        }
        String manaCost = spell.getCard().getManaCost().replace("{X}", "{" + spell.getXValue() + "}");
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, List.of(new MayPayManaEffect(manaCost,
                    new CounterSpellEffect(), "Pay " + manaCost + " to counter " + spell.getCard().getName() + "?",
                    MayPayPayer.ANY_OTHER_PLAYER)));
        }
    }
}
