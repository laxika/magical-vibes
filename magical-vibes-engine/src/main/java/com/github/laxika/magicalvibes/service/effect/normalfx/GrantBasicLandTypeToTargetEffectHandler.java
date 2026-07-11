package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GrantBasicLandTypeToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantBasicLandTypeToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantBasicLandTypeToTargetEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (e.fixedSubtype() == null) {
            playerInputService.beginAddBasicLandTypeChoice(gameData, entry.getControllerId(), target.getId(), e.duration(), e.replacing());
            return;
        }

        applyBasicLandType(target, e.fixedSubtype(), e.duration(), e.replacing());
        gameBroadcastService.logAndBroadcast(gameData, describeBasicLandTypeChange(
                target, e.fixedSubtype(), e.duration(), e.replacing()));
    }

    /**
     * Applies a basic land type change to {@code targetLand}. When {@code replacing} is {@code false}
     * the subtype is added in addition to the land's other types (and its intrinsic mana ability is
     * granted). When {@code replacing} is {@code true} the land <em>becomes</em> the subtype until end
     * of turn, replacing its other land types/mana ability via a transient override (rule 305.7).
     * Shared by this handler (fixed-type path) and the player's basic-land-type choice resolution.
     */
    public static void applyBasicLandType(Permanent targetLand, CardSubtype subtype, EffectDuration duration, boolean replacing) {
        if (replacing) {
            // Type-replacing "becomes" (Tideshaper Mystic): the transient override is read by the
            // static-bonus system (subtypes/mana) and cleared at end of turn by resetModifiers().
            targetLand.setTransientLandTypeOverride(subtype);
            return;
        }

        ManaColor manaColor = EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(subtype);
        ActivatedAbility manaAbility = new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(manaColor)),
                "{T}: Add {" + manaColor.getCode() + "}.");

        if (duration == EffectDuration.UNTIL_END_OF_TURN) {
            // Transient: cleared at end of turn by resetModifiers()
            if (!targetLand.getTransientSubtypes().contains(subtype)) {
                targetLand.getTransientSubtypes().add(subtype);
            }
            targetLand.getTemporaryActivatedAbilities().add(manaAbility);
        } else {
            // Permanent: survives turn resets. Stored on the Permanent, never on the Card —
            // the Card instance is shared with AI simulation copies, so mutating it here
            // would leak a simulated grant into the real game.
            if (!targetLand.getGrantedSubtypes().contains(subtype)) {
                targetLand.getGrantedSubtypes().add(subtype);
            }
            targetLand.getPersistentGrantedActivatedAbilities().add(manaAbility);
        }
    }

    /** Log line describing a basic land type change, shared with the choice-resolution path. */
    public static String describeBasicLandTypeChange(Permanent targetLand, CardSubtype subtype,
                                                     EffectDuration duration, boolean replacing) {
        String durationText = duration == EffectDuration.UNTIL_END_OF_TURN ? " until end of turn" : "";
        String suffix = replacing ? "" : " in addition to its other types";
        return targetLand.getCard().getName() + " becomes a " + subtype.getDisplayName() + suffix + durationText + ".";
    }
}
