package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.ItlimocCradleOfTheSun;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "XLN", collectorNumber = "191")
public class GrowingRitesOfItlimoc extends Card {

    public GrowingRitesOfItlimoc() {
        // Set up back face
        ItlimocCradleOfTheSun backFace = new ItlimocCradleOfTheSun();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When Growing Rites of Itlimoc enters, look at the top four cards of your library.
        // You may reveal a creature card from among them and put it into your hand.
        // Put the rest on the bottom of your library in any order.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(4,
                        new CardTypePredicate(CardType.CREATURE)));

        // At the beginning of your end step, if you control four or more creatures,
        // transform Growing Rites of Itlimoc.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ControlsPermanentCountConditionalEffect(4,
                        new PermanentIsCreaturePredicate(),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "ItlimocCradleOfTheSun";
    }
}
