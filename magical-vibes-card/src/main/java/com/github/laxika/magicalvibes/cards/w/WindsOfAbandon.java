package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouDontControlAndSearchBasicLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "882")
@CardRegistration(set = "SLD", collectorNumber = "1817")
public class WindsOfAbandon extends Card {

    public WindsOfAbandon() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{W}{W}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new ExileTargetPermanentThenEffect(
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        ThenEffectRecipient.TARGET_CONTROLLER),
                new ExileAllCreaturesYouDontControlAndSearchBasicLandsToBattlefieldTappedEffect()));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
