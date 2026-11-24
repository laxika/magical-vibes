package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.VotesReceived;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MobVerdictEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;

@CardRegistration(set = "MKC", collectorNumber = "33")
@CardRegistration(set = "MKC", collectorNumber = "343")
public class MobVerdict extends Card {

    public MobVerdict() {
        addSpellEffect(VoteEffect.forAnotherPlayer().castSecretly());
        addSpellEffect(new MobVerdictEffect());
        addSpellEffect(new DrawCardEffect(VotesReceived.byPlayer()));
    }
}
