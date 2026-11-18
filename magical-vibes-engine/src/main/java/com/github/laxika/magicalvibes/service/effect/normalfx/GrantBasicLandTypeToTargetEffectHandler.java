package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantBasicLandTypeToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantBasicLandTypeToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantBasicLandTypeToTargetEffect) effect;
        if (e.fixedSubtype() == null) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null && !entry.getTargetIds().isEmpty()) {
                target = gameQueryService.findPermanentById(gameData, entry.getTargetIds().getFirst());
            }
            if (target == null) {
                return;
            }
            playerInputService.beginAddBasicLandTypeChoice(
                    gameData, entry.getControllerId(), target.getId(), e.duration(), e.replacing(), e.allowedTypes());
            return;
        }

        for (UUID targetId : targetIds(entry)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            applyBasicLandType(gameData, target, e.fixedSubtype(), e.duration(), e.replacing(),
                    entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId());
            gameLogService.append(gameData, GameLog.text(describeBasicLandTypeChange(
                    target, e.fixedSubtype(), e.duration(), e.replacing())));
        }
    }

    private static List<UUID> targetIds(StackEntry entry) {
        if (!entry.getTargetIds().isEmpty()) {
            return entry.getTargetIds();
        }
        return entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
    }

    /**
     * Applies a basic land type change to {@code targetLand}. When {@code replacing} is {@code false}
     * the subtype is added in addition to the land's other types (and its intrinsic mana ability is
     * granted). When {@code replacing} is {@code true} the land <em>becomes</em> the subtype for the
     * duration, replacing its other land types/mana ability (rule 305.7). Both are timestamped
     * layer-4 floating effects (rules 613.1d, 613.7); the one-shot added type and its mana
     * ability end at end of turn, a permanent addition is an indefinite floating grant (rule 611.2a).
     * Shared by this handler (fixed-type path) and the player's basic-land-type choice resolution.
     */
    public static void applyBasicLandType(GameData gameData, Permanent targetLand, CardSubtype subtype,
                                          Duration duration, boolean replacing, String sourceName,
                                          UUID sourcePermanentId, UUID controllerId) {
        if (replacing) {
            Duration floatingDuration = Duration.STATIC.equals(duration) ? Duration.PERMANENT : duration;
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceName, sourcePermanentId, controllerId,
                    new GrantSubtypeEffect(subtype, GrantScope.TARGET, true),
                    targetLand.getId(), null, null, floatingDuration, 0));
            return;
        }

        ManaColor manaColor = EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(subtype);
        ActivatedAbility manaAbility = new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(manaColor)),
                "{T}: Add {" + manaColor.getCode() + "}.");

        if (Duration.UNTIL_END_OF_TURN.equals(duration)) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceName, sourcePermanentId, controllerId,
                    new GrantSubtypeEffect(subtype, GrantScope.TARGET),
                    targetLand.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
            gameData.grantAbilityToPermanent(sourceName, sourcePermanentId, controllerId, manaAbility,
                    targetLand.getId(), Duration.UNTIL_END_OF_TURN);
        } else {
            gameData.addEffectOnPermanent(sourceName, sourcePermanentId, controllerId,
                    new GrantSubtypeEffect(subtype, GrantScope.TARGET), targetLand.getId(), Duration.PERMANENT);
            gameData.grantAbilityToPermanent(sourceName, sourcePermanentId, controllerId, manaAbility,
                    targetLand.getId(), Duration.PERMANENT);
        }
    }

    /** Log line describing a basic land type change, shared with the choice-resolution path. */
    public static String describeBasicLandTypeChange(Permanent targetLand, CardSubtype subtype,
                                                     Duration duration, boolean replacing) {
        String durationText = Duration.UNTIL_END_OF_TURN.equals(duration) ? " until end of turn"
                : Duration.UNTIL_CONTROLLERS_NEXT_UNTAP_STEP.equals(duration)
                ? " until its controller's next untap step" : "";
        String suffix = replacing ? "" : " in addition to its other types";
        return targetLand.getCard().getName() + " becomes a " + subtype.getDisplayName() + suffix + durationText + ".";
    }
}
