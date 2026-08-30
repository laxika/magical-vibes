package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "195")
public class RelentlessPursuit extends Card {

    public RelentlessPursuit() {
        addEffect(EffectSlot.SPELL,
                new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                        4, CardType.CREATURE, CardType.LAND, List.of(), LookDestination.GRAVEYARD, true));
    }
}
