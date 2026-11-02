package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "12")
public class MirrorSigilSergeant extends Card {

    public MirrorSigilSergeant() {
        // Trample (keyword, auto-loaded from Scryfall).
        // At the beginning of your upkeep, if you control a blue permanent, you may create
        // a token that's a copy of this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentColorInPredicate(Set.of(CardColor.BLUE))),
                new MayEffect(new CreateTokenCopyOfSourceEffect(),
                        "Create a token that's a copy of Mirror-Sigil Sergeant?")));
    }
}
