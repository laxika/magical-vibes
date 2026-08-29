package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "103")
public class GruesomeRealization extends Card {

    public GruesomeRealization() {
        var opponentCreatures = new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You draw two cards and you lose 2 life",
                        List.of(new DrawCardEffect(2), new LoseLifeEffect(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures your opponents control get -1/-1 until end of turn",
                        new BoostAllCreaturesEffect(-1, -1, opponentCreatures))
        )));
    }
}
