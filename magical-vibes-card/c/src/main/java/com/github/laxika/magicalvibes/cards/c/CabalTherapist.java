package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "80")
public class CabalTherapist extends Card {

    public CabalTherapist() {
        // At the beginning of your first main phase, you may sacrifice a creature. When you do,
        // choose a nonland card name, then target player reveals their hand and discards all cards
        // with that name.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        new ChooseNameRevealHandDiscardMatchingEffect(List.of(CardType.LAND)),
                        "a creature"),
                "Sacrifice a creature?"));
    }
}
