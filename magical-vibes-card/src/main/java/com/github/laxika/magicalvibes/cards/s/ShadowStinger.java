package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "123")
public class ShadowStinger extends Card {

    public ShadowStinger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ROGUE), true, false),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)
                ),
                "Tap another untapped Rogue you control: This creature gains deathtouch until end of turn."
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MillEffect(3, MillRecipient.TARGET_PLAYER));
    }
}
