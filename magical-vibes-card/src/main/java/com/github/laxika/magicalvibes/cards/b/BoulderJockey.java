package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LandDropCost;
import com.github.laxika.magicalvibes.model.effect.MayPayLandDropEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "324")
@CardRegistration(set = "MB2", collectorNumber = "560")
public class BoulderJockey extends Card {

    public BoulderJockey() {
        addEffect(EffectSlot.SPELL, new LandDropCost());
        addEffect(EffectSlot.ON_ATTACK, new MayPayLandDropEffect(
                SequenceEffect.of(
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Boulder",
                                3,
                                3,
                                null,
                                null,
                                List.of(CardSubtype.CONSTRUCT),
                                Set.of(),
                                Set.of(CardType.ARTIFACT),
                                true,
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()),
                        new SacrificeCreatedPermanentsAtEndStepEffect()),
                "Pay {D} to create a Boulder?"));
    }
}
