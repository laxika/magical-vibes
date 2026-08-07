package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

/**
 * At the beginning of each player's upkeep, if that player controls a nonblack, nonland permanent,
 * this creature deals 1 damage to that player.
 */
@CardRegistration(set = "WTH", collectorNumber = "85")
public class UrborgStalker extends Card {

    public UrborgStalker() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                        new PermanentNotPredicate(new PermanentIsLandPredicate())))),
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER)));
    }
}
