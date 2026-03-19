package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "275")
public class ChandraBoldPyromancer extends Card {

    public ChandraBoldPyromancer() {
        // +1: Add {R}{R}. Chandra deals 2 damage to target player.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AwardManaEffect(ManaColor.RED, 2), new DealDamageToTargetPlayerEffect(2)),
                "+1: Add {R}{R}. Chandra, Bold Pyromancer deals 2 damage to target player."
        ));

        // −3: Chandra deals 3 damage to target creature or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(3)),
                "\u22123: Chandra, Bold Pyromancer deals 3 damage to target creature or planeswalker."
        ));

        // −7: Chandra deals 10 damage to target player and each creature and planeswalker they control.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new DealDamageToTargetPlayerEffect(10),
                        new DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect(10)),
                "\u22127: Chandra, Bold Pyromancer deals 10 damage to target player and each creature and planeswalker they control."
        ));
    }
}
