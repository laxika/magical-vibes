package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerNameCardRevealTopEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "10")
public class PetraSphinx extends Card {

    public PetraSphinx() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new TargetPlayerNameCardRevealTopEffect(0)),
                "{T}: Target player chooses a card name, then reveals the top card of their library. "
                        + "If that card has the chosen name, that player puts it into their hand. "
                        + "If it doesn't, the player puts it into their graveyard."));
    }
}
