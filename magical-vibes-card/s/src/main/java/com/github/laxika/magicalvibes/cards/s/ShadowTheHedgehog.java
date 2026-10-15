package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSplitSecondToControllerSpellsUsingArtifactManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2086")
public class ShadowTheHedgehog extends Card {

    public ShadowTheHedgehog() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardKeywordPredicate(Keyword.FLASH),
                                new CardKeywordPredicate(Keyword.HASTE))),
                        new DrawCardEffect(1)));
        addEffect(EffectSlot.STATIC, new GrantSplitSecondToControllerSpellsUsingArtifactManaEffect());
    }
}
