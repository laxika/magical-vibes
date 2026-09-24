package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "149")
public class Twinferno extends Card {

    public Twinferno() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "When you cast your next instant or sorcery spell this turn, copy that spell. You may choose new targets for the copy.",
                        new CopyNextInstantOrSorceryCastThisTurnEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gains double strike until end of turn.",
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET),
                        TargetFilters.creatureYouControl())
        )));
    }
}
