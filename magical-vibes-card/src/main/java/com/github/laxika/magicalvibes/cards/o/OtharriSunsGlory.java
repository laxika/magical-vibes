package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONC", collectorNumber = "3")
@CardRegistration(set = "ONC", collectorNumber = "39")
public class OtharriSunsGlory extends Card {

    public OtharriSunsGlory() {
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new ExperienceCountersEffect(1),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        new ControllerExperienceCounters(),
                        "Rebel", 2, 2, CardColor.RED, null,
                        List.of(CardSubtype.REBEL), Set.of(), Set.of(),
                        true, false, Map.of(), List.of(), false, false, false, 0, Set.of())));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{W}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.REBEL)),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()),
                "{2}{R}{W}, Tap an untapped Rebel you control: Return this card from your graveyard to the battlefield tapped."
        ));
    }
}
