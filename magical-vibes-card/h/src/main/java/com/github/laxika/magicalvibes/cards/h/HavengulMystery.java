package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfWhenChosenPermanentLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

public class HavengulMystery extends Card {

    public HavengulMystery() {
        // When this land transforms into Havengul Mystery, return target creature card from your
        // graveyard to the battlefield.
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .linkToSource(true)
                .build());

        // When the creature put onto the battlefield with Havengul Mystery leaves the battlefield,
        // transform Havengul Mystery.
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER),
                new TransformSelfWhenChosenPermanentLeavesEffect());

        // {T}, Pay 1 life: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.BLACK)),
                "{T}, Pay 1 life: Add {B}."
        ));
    }
}
