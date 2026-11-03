package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "233")
@CardRegistration(set = "ECL", collectorNumber = "376")
public class MaralenFaeAscendant extends Card {

    public MaralenFaeAscendant() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));

        var elfOrFaerie = new PermanentAnyOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.ELF),
                new PermanentHasSubtypePredicate(CardSubtype.FAERIE)));
        ExileTopCardsToSourceEffect exileTopTwo = new ExileTopCardsToSourceEffect(2, false, false,
                LibraryScope.TARGET_OPPONENT, true);

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, elfOrFaerie)),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                exileTopTwo);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                elfOrFaerie,
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                exileTopTwo);

        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false,
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                false,
                false,
                0,
                new PermanentCount(new PermanentHasAnySubtypePredicate(
                        Set.of(CardSubtype.ELF, CardSubtype.FAERIE)), CountScope.CONTROLLER),
                true,
                true,
                true));
    }
}
