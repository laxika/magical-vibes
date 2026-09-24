package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMID", collectorNumber = "21")
public class RimewallProtector extends Card {

    public RimewallProtector() {
        CardAnyOfPredicate giantOrWizardCard = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.GIANT),
                new CardSubtypePredicate(CardSubtype.WIZARD)));
        PermanentAnyOfPredicate giantOrWizardPermanent = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.GIANT),
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantKeywordToMatchingCardsEffect(
                        giantOrWizardCard, giantOrWizardPermanent, Set.of(Keyword.WARD),
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(1)));
        // Ward {1}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(1));
    }
}
