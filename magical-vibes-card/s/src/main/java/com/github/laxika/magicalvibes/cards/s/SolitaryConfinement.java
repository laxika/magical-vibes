package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "24")
@CardRegistration(set = "MH2", collectorNumber = "265")
public class SolitaryConfinement extends Card {

    public SolitaryConfinement() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificeUnlessDiscardCardTypeEffect(null));
        addStaticEffect(new SkipDrawStepEffect());
        addStaticEffect(new GrantControllerKeywordEffect(Keyword.SHROUD));
        addStaticEffect(new PreventAllDamageToControllerEffect());
    }
}
