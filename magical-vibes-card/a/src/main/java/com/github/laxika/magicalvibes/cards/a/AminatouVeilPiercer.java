package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantMiracleReducedByManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "1")
public class AminatouVeilPiercer extends Card {

    public AminatouVeilPiercer() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SurveilEffect(2));
        addStaticEffect(new GrantMiracleReducedByManaCostEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), 4));
    }
}
