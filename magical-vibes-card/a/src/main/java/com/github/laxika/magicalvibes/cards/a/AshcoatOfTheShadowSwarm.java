package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PZA", collectorNumber = "6")
public class AshcoatOfTheShadowSwarm extends Card {

    public AshcoatOfTheShadowSwarm() {
        var rat = new PermanentHasSubtypePredicate(CardSubtype.RAT);
        var ratsYouControl = new PermanentCount(rat, CountScope.CONTROLLER);
        var otherRats = new PermanentAllOfPredicate(List.of(
                rat,
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        var attackOrBlockBoost = new BoostAllOwnCreaturesEffect(ratsYouControl, ratsYouControl, otherRats);
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, attackOrBlockBoost);

        var ratCreatureCard = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.RAT)));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new MillEffect(4, MillRecipient.CONTROLLER),
                        new ReturnCardsFromControllerGraveyardToHandEffect(ratCreatureCard, new Fixed(2))
                ),
                "Mill four cards?"
        ));
    }
}
