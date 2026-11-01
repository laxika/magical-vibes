package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "29")
@CardRegistration(set = "MKC", collectorNumber = "339")
public class UnshakableTail extends Card {

    public UnshakableTail() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                CreateTokenEffect.ofClueToken(1));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                                "Sacrifice a Clue"),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()),
                "{2}, Sacrifice a Clue: Return this card from your graveyard to your hand."
        ));
    }
}
