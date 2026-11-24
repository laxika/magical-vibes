package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterUntappedEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "19")
public class VigorousFarming extends Card {

    public VigorousFarming() {
        addStaticEffect(ControlledPermanentsEnterUntappedEffect.lands());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PerpetuallyGrantAbilitiesEffect(
                        Subjects.topmostCardInYourLibrary(new CardTypePredicate(CardType.LAND)),
                        Triggers.youTapThisLandForMana(), new AddManaWhenTappedForManaEffect(ManaColor.GREEN)));
    }
}
