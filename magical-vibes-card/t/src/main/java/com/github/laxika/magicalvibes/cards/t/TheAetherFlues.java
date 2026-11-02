package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "2")
@CardRegistration(set = "OPCA", collectorNumber = "10")
@CardRegistration(set = "MOC", collectorNumber = "139")
public class TheAetherFlues extends Card {

    public TheAetherFlues() {
        MayEffect sacrificeThenReveal = new MayEffect(
                new SacrificePermanentsThenEffect(
                        1,
                        new PermanentIsCreaturePredicate(),
                        new RevealUntilCreatureToBattlefieldRestToLibraryEffect(),
                        "a creature"),
                "Sacrifice a creature?");
        addTrigger(Triggers.planeswalkToThis(), sacrificeThenReveal);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), sacrificeThenReveal);
        addTrigger(Triggers.chaosEnsues(), new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                "Put a creature card from your hand onto the battlefield?"));
    }
}
