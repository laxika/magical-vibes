package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "230")
@CardRegistration(set = "MMA", collectorNumber = "154")
@CardRegistration(set = "C14", collectorNumber = "206")
@CardRegistration(set = "C20", collectorNumber = "183")
@CardRegistration(set = "KHC", collectorNumber = "69")
@CardRegistration(set = "CMA", collectorNumber = "127")
public class MaskedAdmirers extends Card {

    public MaskedAdmirers() {
        // When this creature enters, draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());

        // Whenever you cast a creature spell, you may pay {G}{G}. If you do,
        // return this card from your graveyard to your hand.
        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayPayManaEffect("{G}{G}", ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(), "Pay {G}{G} to return Masked Admirers from your graveyard to your hand?"));
    }
}
