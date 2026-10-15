package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToControllerAndRedirectToTargetPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "37")
public class VengefulArchon extends Card {

    public VengefulArchon() {
        addActivatedAbility(new ActivatedAbility(false, "{X}",
                List.of(new PreventXDamageToControllerAndRedirectToTargetPlayerEffect()),
                "{X}: Prevent the next X damage that would be dealt to you this turn. If damage is prevented this way, Vengeful Archon deals that much damage to target player or planeswalker."));
    }
}
