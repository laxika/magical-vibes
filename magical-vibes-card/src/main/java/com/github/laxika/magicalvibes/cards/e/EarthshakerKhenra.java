package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "90")
public class EarthshakerKhenra extends Card {

    public EarthshakerKhenra() {
        // Haste is an auto-loaded keyword; no engine wiring needed here.

        // When this creature enters, target creature with power less than or equal to this creature's
        // power can't block this turn. The power comparison is source-relative, so a 4/4 Eternalize
        // token can target creatures with power up to 4.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtMostSourcePowerPredicate()
                )),
                "Target must be a creature with power less than or equal to Earthshaker Khenra's power"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CantBlockThisTurnEffect(TapUntapScope.TARGET));

        // Eternalize {4}{R}{R} ({4}{R}{R}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Jackal Warrior with no mana cost. Eternalize only
        // as a sorcery.)
        addEternalize("{4}{R}{R}", "Jackal Warrior");
    }
}
