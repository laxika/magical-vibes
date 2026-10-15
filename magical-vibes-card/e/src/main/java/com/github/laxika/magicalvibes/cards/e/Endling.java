package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "89")
public class Endling extends Card {

    public Endling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)),
                "{B}: This creature gains menace until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{B}: This creature gains deathtouch until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.UNDYING, GrantScope.SELF)),
                "{B}: This creature gains undying until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "This creature gets +1/-1 until end of turn",
                                new BoostSelfEffect(1, -1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "This creature gets -1/+1 until end of turn",
                                new BoostSelfEffect(-1, 1))
                ))),
                "{1}: This creature gets +1/-1 or -1/+1 until end of turn."
        ).withModalChoiceAtActivation());
    }
}
