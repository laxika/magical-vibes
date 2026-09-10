package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "255")
public class TheVision extends Card {

    public TheVision() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new ChooseModeNotYetChosenThisTurnOnSpellCastEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Solar Beam — The Vision gains double strike until end of turn.",
                                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Density Control — The Vision gains indestructible until end of turn.",
                                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Technopathy — Draw a card.", new DrawCardEffect(1))
                        )));
    }
}
