package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.model.effect.EmblemUpkeepTriggerEffect;

import java.util.List;

/**
 * Chandra, Roaring Flame — back face of Chandra, Fire of Kaladesh.
 * Legendary Planeswalker — Chandra (Red).
 */
public class ChandraRoaringFlame extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of your upkeep, this emblem deals 3 damage to you.";

    public ChandraRoaringFlame() {
        // +1: Chandra, Roaring Flame deals 2 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(2)),
                "+1: Chandra, Roaring Flame deals 2 damage to target player or planeswalker."
        ));

        // −2: Chandra, Roaring Flame deals 2 damage to target creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "−2: Chandra, Roaring Flame deals 2 damage to target creature."
        ));

        // −7: Chandra deals 6 damage to each opponent. Each player dealt damage this way gets an emblem
        // with "At the beginning of your upkeep, this emblem deals 3 damage to you." The emblem goes
        // only to opponents whose damage was not fully prevented, so it reads the damage this same
        // resolution actually dealt rather than re-deriving the set of opponents.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new DealDamageToPlayersEffect(6, DamageRecipient.EACH_OPPONENT),
                        new CreateEmblemEffect(
                                List.of(new EmblemUpkeepTriggerEffect(
                                        List.of(new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)),
                                        EMBLEM_TEXT)),
                                EMBLEM_TEXT,
                                EmblemRecipient.EACH_PLAYER_DEALT_DAMAGE_THIS_WAY)
                ),
                "−7: Chandra, Roaring Flame deals 6 damage to each opponent. Each player dealt "
                        + "damage this way gets an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
