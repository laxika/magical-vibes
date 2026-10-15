package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "107")
public class GohamDjinn extends Card {

    public GohamDjinn() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Goham Djinn."));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ColorMostCommonAmongAllPermanents(CardColor.BLACK),
                new StaticBoostEffect(-2, -2, GrantScope.SELF)));
    }
}
