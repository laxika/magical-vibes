package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "42")
public class JodahsAvenger extends Card {

    public JodahsAvenger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new BoostSelfEffect(-1, -1),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Double strike",
                                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption("Protection from red",
                                        new GrantProtectionFromColorUntilEndOfTurnEffect(
                                                CardColor.RED, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption("Vigilance",
                                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption("Shadow",
                                        new GrantKeywordEffect(Keyword.SHADOW, GrantScope.SELF))))),
                "{0}: Until end of turn, this creature gets -1/-1 and gains your choice of double strike, protection from red, vigilance, or shadow."
        ));
    }
}
