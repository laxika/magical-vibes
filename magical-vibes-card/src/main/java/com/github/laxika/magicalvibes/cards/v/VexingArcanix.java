package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerNameCardRevealTopEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "319")
public class VexingArcanix extends Card {

    public VexingArcanix() {
        addActivatedAbility(new ActivatedAbility(true, "{3}", List.of(new TargetPlayerNameCardRevealTopEffect(2)),
                "{3}, {T}: Target player chooses a card name, then reveals the top card of their library. "
                        + "If that card has the chosen name, that player puts it into their hand. Otherwise, "
                        + "they put it into their graveyard and Vexing Arcanix deals 2 damage to them."));
    }
}
