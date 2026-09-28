package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CaitCageBrawlerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "PIP", collectorNumber = "96")
@CardRegistration(set = "PIP", collectorNumber = "409")
@CardRegistration(set = "PIP", collectorNumber = "624")
@CardRegistration(set = "PIP", collectorNumber = "937")
public class CaitCageBrawler extends Card {

    public CaitCageBrawler() {
        // During your turn, Cait has indestructible.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        // Whenever Cait attacks, you and defending player each draw a card, then discard a card.
        // Cait gets two +1/+1 counters if your discard has the greatest mana value or is tied.
        addEffect(EffectSlot.ON_ATTACK, new CaitCageBrawlerEffect());
    }
}
