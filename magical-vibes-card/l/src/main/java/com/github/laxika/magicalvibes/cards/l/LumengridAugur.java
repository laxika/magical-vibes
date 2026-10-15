package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "39")
public class LumengridAugur extends Card {

    public LumengridAugur() {
        // {1}, {T}: Target player draws a card, then discards a card. If that player discards an
        // artifact card this way, untap this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new TargetPlayerDrawsThenDiscardsUntapSelfIfCardTypeEffect(CardType.ARTIFACT)),
                "{1}, {T}: Target player draws a card, then discards a card. If that player discards an artifact card this way, untap this creature."
        ));
    }
}
