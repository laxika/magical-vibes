package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceDownEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceUpCopyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "90")
public class VesuvanShapeshifter extends Card {

    public VesuvanShapeshifter() {
        TurnSourceFaceDownEffect turnFaceDown = new TurnSourceFaceDownEffect();
        MayEffect turnFaceDownMay = new MayEffect(turnFaceDown, "Turn this creature face down?");
        TurnFaceUpCopyEffect turnFaceUpCopy = new TurnFaceUpCopyEffect(
                new PermanentIsCreaturePredicate(),
                Map.of(EffectSlot.UPKEEP_TRIGGERED, List.of(turnFaceDownMay)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(),
                Map.of(EffectSlot.ON_TURNED_FACE_UP, List.of(turnFaceUpCopy),
                        EffectSlot.UPKEEP_TRIGGERED, List.of(turnFaceDownMay))));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, turnFaceUpCopy);
        addMorph("{1}{U}");
    }
}
