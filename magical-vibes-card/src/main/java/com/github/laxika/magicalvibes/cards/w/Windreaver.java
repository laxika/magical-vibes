package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "138")
public class Windreaver extends Card {

    public Windreaver() {
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "{W}: This creature gains vigilance until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new BoostSelfEffect(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new SwitchPowerToughnessEffect(true)),
                "{U}: Switch this creature's power and toughness until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(ReturnToHandEffect.self()),
                "{U}: Return this creature to its owner's hand."));
    }
}
