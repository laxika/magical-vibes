package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ICE", collectorNumber = "93")
public class RayOfErasure extends Card {

    public RayOfErasure() {
        // "Target player mills a card."
        addSpellEffect(new MillEffect(1, Subjects.targetPlayer()));
        // "Draw a card at the beginning of the next turn's upkeep."
        addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
