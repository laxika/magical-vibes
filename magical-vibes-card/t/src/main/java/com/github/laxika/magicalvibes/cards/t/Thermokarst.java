package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Thermokarst — {1}{G}{G} Sorcery.
 * Destroy target land. If that land was a snow land, you gain 1 life.
 */
@CardRegistration(set = "ICE", collectorNumber = "268")
@CardRegistration(set = "ME2", collectorNumber = "183")
public class Thermokarst extends Card {

    public Thermokarst() {
        // The destroy runs first; the "was a snow land" check then reads the destroyed land's last
        // known information (CR 608.2h), so a life-gain prevention effect the land itself provided
        // no longer applies when the life gain happens.
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                        new GainLifeEffect(1)));
    }
}
