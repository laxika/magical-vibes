package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;


@CardRegistration(set = "IKO", collectorNumber = "188")
@CardRegistration(set = "PIP", collectorNumber = "217")
@CardRegistration(set = "PIP", collectorNumber = "745")
public class GeneralsEnforcer extends Card {

    public GeneralsEnforcer() {
        addStaticEffect(new StaticBoostEffect(
                0,
                0,
                Set.of(Keyword.INDESTRUCTIBLE),
                Subjects.creaturesYouControl(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{B}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.CREATURE)),
                                new CreateTokenEffect(
                                        "Human Soldier",
                                        1,
                                        1,
                                        CardColor.WHITE,
                                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                        Set.of(),
                                        Set.of()))),
                "{2}{W}{B}: Exile target card from a graveyard. If it was a creature card, create a 1/1 white Human Soldier creature token."
        ));
    }
}
