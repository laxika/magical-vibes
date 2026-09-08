package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.KroxaDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessEscapedEffect;
import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "221")
public class KroxaTitanOfDeathsHunger extends Card {

    public KroxaTitanOfDeathsHunger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfUnlessEscapedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KroxaDiscardEffect());
        addEffect(EffectSlot.ON_ATTACK, new KroxaDiscardEffect());

        addCastingOption(new GraveyardCast(null, "{B}{B}{R}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, false, true));
    }
}
