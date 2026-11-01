package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ApplyLudevicCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AsTransformsIntoThisEffect;

public class OlagLudevicsHubris extends Card {

    public OlagLudevicsHubris() {
        addStaticEffect(new AsTransformsIntoThisEffect(new ApplyLudevicCopyEffect()));
    }
}
