package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "63")
@CardRegistration(set = "PIP", collectorNumber = "334")
@CardRegistration(set = "PIP", collectorNumber = "591")
@CardRegistration(set = "PIP", collectorNumber = "862")
public class MysteriousStranger extends Card {

    public MysteriousStranger() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));

        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE);
        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect.forRandomSingleCopy());
    }
}
