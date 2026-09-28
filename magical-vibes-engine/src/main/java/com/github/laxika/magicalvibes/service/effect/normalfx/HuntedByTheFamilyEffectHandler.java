package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.HuntedByTheFamilyEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentBecomesSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Hunted by The Family one target at a time, asking each target's controller to choose. */
@Component
@RequiredArgsConstructor
public class HuntedByTheFamilyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SetCardTypesEffectHandler setCardTypesEffectHandler;
    private final SetTargetColorEffectHandler setTargetColorEffectHandler;
    private final TargetPermanentBecomesSubtypeEffectHandler subtypeEffectHandler;
    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;
    private final LosesAllAbilitiesEffectHandler losesAllAbilitiesEffectHandler;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyEffectHandler;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HuntedByTheFamilyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(entry.targetsForEffect(effect));
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            UUID targetId = state.currentTargetId;
            if (HuntedByTheFamilyEffect.HUMAN_OPTION.equals(chosen)) {
                transformTarget(gameData, entry, targetId);
            } else if (HuntedByTheFamilyEffect.COPY_OPTION.equals(chosen)) {
                tokenCopyEffectHandler.resolveForTarget(gameData, entry,
                        new CreateTokenCopyOfTargetPermanentEffect(), targetId);
            }
            advance(gameData, entry);
            return;
        }

        advance(gameData, entry);
    }

    private void advance(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        while (!state.remaining.isEmpty()) {
            UUID targetId = state.remaining.removeFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId == null) {
                continue;
            }

            state.currentTargetId = targetId;
            state.currentPlayerId = controllerId;
            gameData.rerunCurrentEffectAfterInteraction = true;
            villainousChoiceSupport.beginChoice(gameData, controllerId, entry.getCard().getName(),
                    HuntedByTheFamilyEffect.HUMAN_OPTION,
                    List.of(HuntedByTheFamilyEffect.HUMAN_OPTION,
                            HuntedByTheFamilyEffect.COPY_OPTION),
                    entry.getCard().getName() + " - Choose a villainous choice.");
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void transformTarget(GameData gameData, StackEntry entry, UUID targetId) {
        if (targetId == null || gameQueryService.findPermanentById(gameData, targetId) == null) {
            return;
        }
        StackEntry targetEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(), targetId, entry.getSourcePermanentId());
        targetEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        setCardTypesEffectHandler.resolve(gameData, targetEntry,
                new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.TARGET,
                        EffectDuration.PERMANENT));
        setTargetColorEffectHandler.resolve(gameData, targetEntry, new SetTargetColorEffect(CardColor.WHITE));
        subtypeEffectHandler.resolve(gameData, targetEntry,
                new TargetPermanentBecomesSubtypeEffect(CardSubtype.HUMAN));
        setBasePowerToughnessEffectHandler.resolve(gameData, targetEntry,
                new SetBasePowerToughnessEffect(1, 1, GrantScope.TARGET,
                        EffectDuration.PERMANENT));
        losesAllAbilitiesEffectHandler.resolve(gameData, targetEntry,
                new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT));
    }
}
