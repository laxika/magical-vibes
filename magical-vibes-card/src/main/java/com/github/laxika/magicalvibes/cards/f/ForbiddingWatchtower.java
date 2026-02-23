package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "352")
public class ForbiddingWatchtower extends Card {

    public ForbiddingWatchtower() {
        setEntersTapped(true);
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new AnimateLandEffect(1, 5, List.of(CardSubtype.SOLDIER), Set.of(), CardColor.WHITE)),
                "{1}{W}: Forbidding Watchtower becomes a 1/5 white Soldier creature until end of turn. It's still a land."
        ));
    }
}
