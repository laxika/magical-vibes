package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;

@CardRegistration(set = "VMA", collectorNumber = "87")
@CardRegistration(set = "GN3", collectorNumber = "33")
@CardRegistration(set = "LTC", collectorNumber = "195")
public class PleaForPower extends Card {

    public static final String TIME = "Time";
    public static final String KNOWLEDGE = "Knowledge";

    public PleaForPower() {
        addSpellEffect(VoteEffect.startingWithYou(TIME, KNOWLEDGE));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(TIME), new ControllerExtraTurnEffect(1)));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(KNOWLEDGE), new DrawCardEffect(3)));
    }
}
