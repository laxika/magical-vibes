package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "152")
@CardRegistration(set = "EMA", collectorNumber = "219")
@CardRegistration(set = "MPS", collectorNumber = "9")
@CardRegistration(set = "2XM", collectorNumber = "240")
@CardRegistration(set = "SPG", collectorNumber = "92")
@CardRegistration(set = "SPG", collectorNumber = "102")
@CardRegistration(set = "SLZ", collectorNumber = "98")
@CardRegistration(set = "SLZ", collectorNumber = "219")
@CardRegistration(set = "SLZ", collectorNumber = "340")
public class ChromeMox extends Card {

    public ChromeMox() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ExileFromHandToImprintEffect(
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        "a nonartifact, nonland card"),
                "You may exile a nonartifact, nonland card from your hand."));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.IMPRINTED_CARD_COLORS)),
                "{T}: Add one mana of any of the exiled card's colors."
        ));
    }
}
