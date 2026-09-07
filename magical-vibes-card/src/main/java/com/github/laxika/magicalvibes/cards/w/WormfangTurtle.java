package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentYouControlAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "JUD", collectorNumber = "60")
public class WormfangTurtle extends Card {

    public WormfangTurtle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExilePermanentYouControlAndTrackWithSourceEffect(new PermanentIsLandPredicate()));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceEffect());
    }
}
