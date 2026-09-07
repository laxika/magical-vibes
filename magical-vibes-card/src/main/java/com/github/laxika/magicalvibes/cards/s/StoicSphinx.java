package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "71")
public class StoicSphinx extends Card {

    public StoicSphinx() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate())),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
    }
}
