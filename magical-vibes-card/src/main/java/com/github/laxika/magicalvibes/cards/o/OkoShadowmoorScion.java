package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemWithChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

/** Back face of Oko, Lorwyn Liege. */
public class OkoShadowmoorScion extends Card {

    public OkoShadowmoorScion() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{U}", new TransformSelfEffect(), "Pay {U} to transform Oko?"));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new MillControllerAndMayReturnMilledPermanentToHandEffect(3)),
                "−1: Mill three cards. You may put a permanent card from among them into your hand."));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new CreateTokenEffect(2, "Elk", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.ELK), Set.of(), Set.of())),
                "−3: Create two 3/3 green Elk creature tokens."));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemWithChosenSubtypeEffect(
                        3,
                        3,
                        Set.of(Keyword.VIGILANCE, Keyword.HEXPROOF),
                        "Creatures you control of the chosen type get +3/+3 and have vigilance and hexproof.")),
                "−6: Choose a creature type. You get an emblem with \"Creatures you control of the chosen type get +3/+3 and have vigilance and hexproof.\""));
    }
}
