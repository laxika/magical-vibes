package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "9")
public class RadiantSolar extends Card {

    public RadiantSolar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect());
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new VentureIntoDungeonEffect());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new VentureIntoDungeonEffect(), new GainLifeEffect(3)),
                "{W}, Discard this card: Venture into the dungeon and you gain 3 life."
        ));
    }
}
