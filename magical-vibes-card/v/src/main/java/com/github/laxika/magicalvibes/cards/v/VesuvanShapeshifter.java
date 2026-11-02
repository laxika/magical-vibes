package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceDownEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceUpCopyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "90")
@CardRegistration(set = "A25", collectorNumber = "77")
@CardRegistration(set = "TSR", collectorNumber = "97")
@CardRegistration(set = "C19", collectorNumber = "101")
public class VesuvanShapeshifter extends Card {

    public VesuvanShapeshifter() {
        TurnSourceFaceDownEffect turnFaceDown = new TurnSourceFaceDownEffect();
        MayEffect turnFaceDownMay = new MayEffect(turnFaceDown, "Turn this creature face down?");
        TurnFaceUpCopyEffect turnFaceUpCopy = new TurnFaceUpCopyEffect(
                new PermanentIsCreaturePredicate(),
                List.of(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), turnFaceDownMay)));

        addStaticEffect(new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(),
                List.of(
                        StaticAbility.of(turnFaceUpCopy),
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), turnFaceDownMay))));
        addStaticEffect(turnFaceUpCopy);
        addMorph("{1}{U}");
    }
}
