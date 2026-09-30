package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YWOE", collectorNumber = "10")
public class HeirToDragonfire extends Card {

    public HeirToDragonfire() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: Heir to Dragonfire gets +1/+0 until end of turn."
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new PerpetuallyGrantCardCharacteristicsEffect(
                                Set.of(), Set.of(CardSubtype.DRAGON), List.of()),
                        new PerpetuallyBoostCardEffect(this, 3, 3),
                        new PerpetuallyGrantKeywordToSourceEffect(Keyword.FLYING)
                ),
                "{2}{R}, Reveal Heir to Dragonfire from your hand: Heir to Dragonfire perpetually becomes a Dragon, "
                        + "gets +3/+3, and gains flying."
        ).withSourceStaysInHand().withRevealsSourceFromHand());
    }
}
