package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesMatchingCantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "35")
public class DeepchannelMentor extends Card {

    public DeepchannelMentor() {
        addStaticEffect(new ControlledCreaturesMatchingCantBeBlockedEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))
        ));
    }
}
