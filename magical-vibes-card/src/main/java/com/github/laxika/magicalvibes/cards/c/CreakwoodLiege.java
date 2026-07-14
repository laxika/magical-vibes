package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "117")
public class CreakwoodLiege extends Card {

    public CreakwoodLiege() {
        // Other black creatures you control get +1/+1. (OWN_CREATURES scope excludes the source itself.)
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));

        // Other green creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));

        // At the beginning of your upkeep, you may create a 1/1 black and green Worm creature token.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CreateTokenEffect("Worm", 1, 1, CardColor.BLACK,
                        Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.WORM)),
                "Create a 1/1 black and green Worm creature token?"));
    }
}
