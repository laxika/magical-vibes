package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "192")
public class InThePresenceOfAges extends Card {

    public InThePresenceOfAges() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                4, CardType.CREATURE, CardType.LAND, List.of(), LookDestination.GRAVEYARD, true));
    }
}
