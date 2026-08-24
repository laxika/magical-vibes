package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ChillingChronicle;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

/**
 * Mysterious Tome — front face of Mysterious Tome // Chilling Chronicle.
 */
@CardRegistration(set = "MID", collectorNumber = "63")
public class MysteriousTome extends Card {

    public MysteriousTome() {
        setBackFaceCard(new ChillingChronicle());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1), new TransformSelfEffect()),
                "{2}, {T}: Draw a card. Transform this artifact."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChillingChronicle";
    }
}
