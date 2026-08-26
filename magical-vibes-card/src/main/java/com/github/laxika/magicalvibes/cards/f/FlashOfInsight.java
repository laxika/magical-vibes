package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileXCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "40")
public class FlashOfInsight extends Card {

    public FlashOfInsight() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new XValue()));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{1}{U}"),
                new ExileXCardsFromGraveyardCastingCost(new CardColorPredicate(CardColor.BLUE), "blue"))));
    }
}
