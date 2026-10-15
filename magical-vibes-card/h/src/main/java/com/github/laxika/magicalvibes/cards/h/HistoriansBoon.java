package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "21")
@CardRegistration(set = "DMC", collectorNumber = "71")
public class HistoriansBoon extends Card {

    public HistoriansBoon() {
        CreateTokenEffect soldier = CreateTokenEffect.whiteSoldier(1);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, soldier);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()), soldier));
        addEffect(EffectSlot.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES,
                new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of()));
    }
}
