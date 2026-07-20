package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "135")
public class HarshMentor extends Card {

    public HarshMentor() {
        // Whenever an opponent activates an ability of an artifact, creature, or land on the
        // battlefield, if it isn't a mana ability, this creature deals 2 damage to that player.
        // The slot fires only on the non-mana activation path (mana abilities are excluded); the
        // conditional wrapper restricts it to artifact/creature/land permanents.
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY, new TriggeringPermanentConditionalEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate())),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)));
    }
}
