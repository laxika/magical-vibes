package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "79")
public class CliffrunnerBehemoth extends Card {

    public CliffrunnerBehemoth() {
        // This creature has haste as long as you control a red permanent.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.RED))),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        // This creature has lifelink as long as you control a white permanent.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));
    }
}
