package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "106")
public class ScarabFeast extends Card {

    public ScarabFeast() {
        // Exile up to three target cards from a single graveyard.
        addEffect(EffectSlot.SPELL, new ExileCardsFromGraveyardEffect(3, 0));

        // Cycling {B} ({B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new DrawCardEffect(1)),
                "Cycling {B} ({B}, Discard this card: Draw a card.)"));
    }
}
