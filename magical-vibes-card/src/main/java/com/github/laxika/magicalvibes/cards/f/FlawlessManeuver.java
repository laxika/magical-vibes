package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "24")
@CardRegistration(set = "CMM", collectorNumber = "459")
@CardRegistration(set = "CMM", collectorNumber = "692")
@CardRegistration(set = "TLE", collectorNumber = "306")
public class FlawlessManeuver extends Card {

    public FlawlessManeuver() {
        addCastingOption(new AlternateHandCast(List.of(), new ControllerControlsCommander(), false));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));
    }
}
