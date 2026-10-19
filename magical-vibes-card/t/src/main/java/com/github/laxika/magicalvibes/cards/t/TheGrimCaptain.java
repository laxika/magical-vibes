package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public class TheGrimCaptain extends Card {

    public TheGrimCaptain() {
        PermanentPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        // One ability: "each opponent sacrifices a nonland permanent. Then you may put ..."
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new SacrificePermanentsEffect(1, nonland, SacrificeRecipient.EACH_OPPONENT),
                new MayEffect(
                        new ReturnCardExiledWithSourceToBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE), false, null, true, true),
                        "Put an exiled creature card used to craft The Grim Captain onto the battlefield "
                                + "under your control tapped and attacking?")));
    }
}
