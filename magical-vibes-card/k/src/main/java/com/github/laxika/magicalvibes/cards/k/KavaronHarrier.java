package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "139")
public class KavaronHarrier extends Card {

    public KavaronHarrier() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{2}",
                SequenceEffect.of(
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Robot",
                                2,
                                2,
                                null,
                                null,
                                List.of(CardSubtype.ROBOT),
                                Set.of(),
                                Set.of(CardType.ARTIFACT),
                                false,
                                true,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()),
                        new MakeCreatedPermanentsAttackingEffect(),
                        new SacrificeCreatedPermanentsAtEndOfCombatEffect()),
                "Pay {2} to create a Robot token?"));
    }
}
