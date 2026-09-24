package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureOutsideBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenMillAndRepeatIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenMillControllerAndRepeatIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SelfBecomesCreatureOutsideBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "977")
@CardRegistration(set = "SLD", collectorNumber = "1417")
@CardRegistration(set = "SLD", collectorNumber = "1977")
@CardRegistration(set = "AA2", collectorNumber = "16")
@CardRegistration(set = "MH2", collectorNumber = "202")
@CardRegistration(set = "MH2", collectorNumber = "306")
public class GristTheHungerTide extends Card {

    public GristTheHungerTide() {
        addEffect(EffectSlot.STATIC, new BecomeCreatureOutsideBattlefieldEffect(
                1, 1, List.of(CardSubtype.INSECT)));

        CreateTokenEffect insectToken = new CreateTokenEffect(
                1, "Insect", 1, 1, CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.INSECT));
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenThenMillControllerAndRepeatIfMilledEffect(
                        insectToken, new CardSubtypePredicate(CardSubtype.INSECT))),
                "+1: Create a 1/1 black and green Insect creature token, then mill a card. "
                        + "If an Insect card was milled this way, put a loyalty counter on Grist and repeat this process."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new MayEffect(new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        new DestroyTargetPermanentEffect(new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate()))),
                        "a creature"), "Sacrifice a creature?")),
                "−2: You may sacrifice a creature. When you do, destroy target creature or planeswalker."
        ));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new LoseLifeEffect(
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                        LoseLifeRecipient.EACH_OPPONENT)),
                "−5: Each opponent loses life equal to the number of creature cards in your graveyard."
        ));
    }
}
