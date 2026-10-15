package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingConundrumDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "63")
public class LivingConundrum extends Card {

    public LivingConundrum() {
        addEffect(EffectSlot.STATIC, new LivingConundrumDrawReplacementEffect());

        NotCondition emptyLibrary = new NotCondition(new CardsInLibraryAtLeast(1));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                emptyLibrary,
                new SetBasePowerToughnessEffect(10, 10, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                emptyLibrary,
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE), GrantScope.SELF)));
    }
}
