package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "658")
public class GrimReapersScythe extends Card {

    public GrimReapersScythe() {
        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD,
                CreateTokenEffect.blackZombie(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .enterWithCounter(CounterType.FINALITY)
                                .enterWithCounterCount(1)
                                .build()
                ),
                "{3}{B}, {T}, Sacrifice two creatures: Return target creature card from your graveyard "
                        + "to the battlefield with a finality counter on it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
