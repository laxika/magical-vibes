package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CounterSpellAndExileThenGrantFreeCastPermissionEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndExileThenGrantFreeCastPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) {
            return;
        }

        CounterSpellAndExileThenGrantFreeCastPermissionEffect counterEffect =
                (CounterSpellAndExileThenGrantFreeCastPermissionEffect) effect;
        if (counterEffect.permanentSpellsOnly() && !isPermanentSpell(targetEntry.getCard())) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        if (!counterSupport.counterSpellAndExile(gameData, entry, targetEntry)) return;

        ExiledCardEntry exiled = gameData.findExiledCard(targetCardId);
        if (exiled == null) return;

        gameData.exilePlayPermissions.put(targetCardId, entry.getControllerId());
        gameData.exilePlayWithoutPayingManaCost.add(targetCardId);
    }

    private boolean isPermanentSpell(Card card) {
        return card.hasType(CardType.CREATURE)
                || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.ENCHANTMENT)
                || card.hasType(CardType.PLANESWALKER)
                || card.hasType(CardType.BATTLE);
    }
}
