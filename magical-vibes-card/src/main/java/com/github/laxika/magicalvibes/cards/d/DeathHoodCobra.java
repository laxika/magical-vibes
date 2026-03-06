package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "108")
public class DeathHoodCobra extends Card {

    public DeathHoodCobra() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new GrantKeywordEffect(Keyword.REACH, GrantScope.SELF)), "{1}{G}: Death-Hood Cobra gains reach until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}", List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)), "{1}{G}: Death-Hood Cobra gains deathtouch until end of turn."));
    }
}
