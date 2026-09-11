package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForExiledCardsWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "39")
public class SkyclaveApparition extends Card {

    public SkyclaveApparition() {
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentMaxManaValuePredicate(4)
        ));
        target(new PermanentPredicateTargetFilter(
                targetPredicate,
                "Target must be a nonland, nontoken permanent you don't control with mana value 4 or less"
        ), 0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentAndTrackWithSourceEffect());

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new CreateTokensForExiledCardsWithSourceEffect(new CreateTokenEffect(
                        "Illusion", 0, 0, CardColor.BLUE,
                        List.of(CardSubtype.ILLUSION), Set.of(), Set.of())));
    }
}
