package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "195")
@CardRegistration(set = "FDN", collectorNumber = "232")
@CardRegistration(set = "M21", collectorNumber = "204")
public class ScavengingOoze extends Card {

    public ScavengingOoze() {
        // {G}: Exile target card from a graveyard. If it was a creature card, put a +1/+1 counter
        // on Scavenging Ooze and you gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new ExileGraveyardCardWithConditionalBonusEffect(1, 1, 0, 0)),
                "{G}: Exile target card from a graveyard. If it was a creature card, put a +1/+1 counter on Scavenging Ooze and you gain 1 life."
        ));
    }
}
