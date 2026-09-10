package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "34")
public class Shiv extends Card {

    public Shiv() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{R}",
                        List.of(new BoostSelfEffect(1, 0)),
                        "{R}: This creature gets +1/+0 until end of turn."
                ),
                GrantScope.ALL_CREATURES
        ));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new CreateTokenEffect(
                "Dragon", 5, 5, CardColor.RED, List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()));
    }
}
