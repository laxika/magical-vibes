package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves restricted mana production from spells and abilities that use the stack. */
@Component
@RequiredArgsConstructor
public class AwardRestrictedManaOfColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardRestrictedManaOfColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mana = (AwardRestrictedManaOfColorsEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, mana.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }
        if (mana.colors().size() == 1) {
            mana.restriction().applyTo(gameData.playerManaPools.get(entry.getControllerId()),
                    mana.colors().getFirst(), amount);
            return;
        }
        var context = new ChoiceContext.RestrictedManaColorChoice(entry.getControllerId(), amount,
                source != null && gameQueryService.isCreature(gameData, source),
                mana.colors(), mana.restriction());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, context,
                mana.colors().stream().map(Enum::name).toList(),
                "Choose a color of mana to add (" + mana.restriction().description() + ")."));
    }
}
