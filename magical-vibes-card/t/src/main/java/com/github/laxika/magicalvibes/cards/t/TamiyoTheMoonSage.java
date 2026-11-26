package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "79")
@CardRegistration(set = "SLD", collectorNumber = "396")
public class TamiyoTheMoonSage extends Card {

    public TamiyoTheMoonSage() {
        // +1: Tap target permanent. It doesn't untap during its controller's next untap step.
        // No target filter: any permanent is legal, so the tap effect's PERMANENT spec governs.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new TapPermanentsEffect(Subjects.target()), new SkipNextUntapEffect(Subjects.target())),
                "+1: Tap target permanent. It doesn't untap during its controller's next untap step."
        ));

        // −2: Draw a card for each tapped creature target player controls.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DrawCardEffect(new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate()
                        )),
                        CountScope.TARGET_PLAYER
                ))),
                "−2: Draw a card for each tapped creature target player controls."
        ));

        // −8: You get an emblem with "You have no maximum hand size" and "Whenever a card is put into
        // your graveyard from anywhere, you may return it to your hand." The hand-size half reuses the
        // rest-of-game grant; the emblem itself carries only the graveyard-return trigger.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new GrantNoMaximumHandSizeEffect(Duration.PERMANENT),
                        new CreateEmblemEffect(
                                new TriggeredAbility(
                                        Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN,
                                                ObjectRelation.YOU_OWN),
                                        new MayEffect(new PutCardsIntoHandEffect(Subjects.triggeringCards()),
                                                "Return that card to your hand?")),
                                "Whenever a card is put into your graveyard from anywhere, you may "
                                        + "return it to your hand.")),
                "−8: You get an emblem with \"You have no maximum hand size\" and \"Whenever a card is"
                        + " put into your graveyard from anywhere, you may return it to your hand.\""
        ));
    }
}
