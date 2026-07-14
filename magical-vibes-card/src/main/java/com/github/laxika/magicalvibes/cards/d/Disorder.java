package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerControllingMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "7ED", collectorNumber = "179")
public class Disorder extends Card {

    public Disorder() {
        // Disorder deals 2 damage to each white creature and each player who controls a white creature.
        var whiteCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));

        // Damage the controllers first so a white creature dying to the mass damage does not
        // spare its controller — the whole spell resolves simultaneously.
        addEffect(EffectSlot.SPELL, new DealDamageToEachPlayerControllingMatchingPermanentEffect(2, whiteCreature));
        addEffect(EffectSlot.SPELL, new DealDamageToEachMatchingPermanentEffect(
                2, whiteCreature, EachPermanentScope.ALL_PLAYERS));
    }
}
