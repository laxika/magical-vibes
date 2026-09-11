package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SilvanRally;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "166")
public class ThranduilSindarinLiege extends Card {

    public ThranduilSindarinLiege() {
        setBackFaceCard(new SilvanRally());
        addCastingOption(new AdventureCast("{1}{G/U}{G/U}"));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ELF)));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Elf", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF), Set.of(), Set.of()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SilvanRally";
    }
}
