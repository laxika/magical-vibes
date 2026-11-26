package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "42")
@CardRegistration(set = "TSR", collectorNumber = "72")
public class JodahsAvenger extends Card {

    public JodahsAvenger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        BoostEffect.self(-1, -1),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Double strike",
                                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.source())),
                                new ChooseOneEffect.ChooseOneOption("Protection from red",
                                        new GrantProtectionFromColorUntilEndOfTurnEffect(
                                                CardColor.RED, Subjects.source())),
                                new ChooseOneEffect.ChooseOneOption("Vigilance",
                                        new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.source())),
                                new ChooseOneEffect.ChooseOneOption("Shadow",
                                        new GrantKeywordEffect(Keyword.SHADOW, Subjects.source()))))),
                "{0}: Until end of turn, this creature gets -1/-1 and gains your choice of double strike, protection from red, vigilance, or shadow."
        ));
    }
}
