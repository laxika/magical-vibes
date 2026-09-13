package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryAndAllowOneMayPlayForLifeEffect;

@CardRegistration(set = "NEO", collectorNumber = "114")
public class NashiMoonSagesScion extends Card {

    public NashiMoonSagesScion() {
        addNinjutsu("{3}{B}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardOfEachPlayersLibraryAndAllowOneMayPlayForLifeEffect());
    }
}
