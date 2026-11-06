package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantEffectToChosenCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToChosenCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantEffectToChosenCreatureUntilEndOfTurnEffect grant =
                (GrantEffectToChosenCreatureUntilEndOfTurnEffect) effect;
        UUID chosenCreatureId = entry.getChosenPermanentId();
        if (chosenCreatureId == null) {
            return;
        }

        Permanent chosenCreature = gameQueryService.findPermanentById(gameData, chosenCreatureId);
        if (chosenCreature == null || !gameQueryService.isCreature(gameData, chosenCreature)) {
            return;
        }

        chosenCreature.addTemporaryAbility(grantedBySource(entry,
                LegacySlots.withEffect(grant.grantedAbility(), grant.grantedEffect())));
    }

    /**
     * Names the source of the resolving ability as the granted triggered ability's granting permanent
     * ({@link TriggeredAbility#grantingPermanentId()}): "transform Optimus Prime" in the ability Optimus Prime gives the
     * chosen creature.
     */
    private static Ability grantedBySource(StackEntry entry, Ability granted) {
        return granted instanceof TriggeredAbility triggered && entry.getSourcePermanentId() != null
                ? triggered.withGrantingPermanentId(entry.getSourcePermanentId())
                : granted;
    }
}
