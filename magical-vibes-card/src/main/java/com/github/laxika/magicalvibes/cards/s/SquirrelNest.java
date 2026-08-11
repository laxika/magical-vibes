package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "274")
public class SquirrelNest extends Card {

    public SquirrelNest() {
        // Enchant land.
        // Enchanted land has "{T}: Create a 1/1 green Squirrel creature token."
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())),
                        "{T}: Create a 1/1 green Squirrel creature token."
                ),
                GrantScope.ENCHANTED_PERMANENT
        ));
    }
}
