package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "TDM", collectorNumber = "36")
public class AgentOfKotis extends Card {

    public AgentOfKotis() {
        // Renew — {3}{U}, exile this card from your graveyard: Put two +1/+1 counters on target creature.
        addScavenge("{3}{U}");
    }
}
