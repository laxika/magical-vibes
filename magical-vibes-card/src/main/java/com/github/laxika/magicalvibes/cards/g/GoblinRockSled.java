package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedDuringControllersLastTurn;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "4ED", collectorNumber = "199")
@CardRegistration(set = "DRK", collectorNumber = "67")
public class GoblinRockSled extends Card {

    public GoblinRockSled() {
        // This creature doesn't untap during your untap step if it attacked during your last turn.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceAttackedDuringControllersLastTurn(), DoesntUntapEffect.self()));

        // This creature can't attack unless defending player controls a Mountain.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                "a Mountain"
        ));
    }
}
