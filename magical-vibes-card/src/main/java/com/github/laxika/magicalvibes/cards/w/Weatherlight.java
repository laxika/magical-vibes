package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "237")
public class Weatherlight extends Card {

    public Weatherlight() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(5, new CardIsHistoricPredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), new AnimateSelfAsCreatureEffect()),
                "Crew 3"
        ));
    }
}
