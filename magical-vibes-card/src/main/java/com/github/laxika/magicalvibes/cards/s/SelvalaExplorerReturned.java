package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ParleyEffect;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "260")
public class SelvalaExplorerReturned extends Card {

    public SelvalaExplorerReturned() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ParleyEffect()),
                "{T}: Each player reveals the top card of their library. For each nonland card revealed this way, add {G} and you gain 1 life. Then each player draws a card. (Activate only as an instant.)"
        ));
    }
}
