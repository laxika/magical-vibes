package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "25")
public class SkylineScout extends Card {

    public SkylineScout() {
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay("{1}{W}", new GrantKeywordEffect(Keyword.FLYING, Subjects.source()))
                .withPrompt("Pay {1}{W} to give Skyline Scout flying until end of turn?"));
    }
}
