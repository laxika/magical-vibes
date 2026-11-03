package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.AmplifyEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "71")
public class GhastlyRemains extends Card {

    public GhastlyRemains() {
        addStaticEffect(new AmplifyEffect(
                1, new CardSubtypePredicate(CardSubtype.ZOMBIE)));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new MayPayManaEffect(
                        "{B}{B}{B}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Pay {B}{B}{B} to return Ghastly Remains to your hand?"));
    }
}
