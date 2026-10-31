package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "233")
@CardRegistration(set = "ECL", collectorNumber = "376")
public class MaralenFaeAscendant extends Card {

    public MaralenFaeAscendant() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));

        CardAnyOfPredicate elfOrFaerie = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.ELF),
                new CardSubtypePredicate(CardSubtype.FAERIE)));
        ExileTopCardsToSourceEffect exileTopTwo = new ExileTopCardsToSourceEffect(2, false, false,
                LibraryScope.TARGET_OPPONENT, true);

        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(elfOrFaerie, exileTopTwo));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(elfOrFaerie,
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                        exileTopTwo));

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
