package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "15")
public class PhyrexianRebirth extends Card {

    public PhyrexianRebirth() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect(
                "Phyrexian Horror",
                List.of(CardSubtype.PHYREXIAN, CardSubtype.HORROR),
                Set.of(CardType.ARTIFACT)
        ));
    }
}
