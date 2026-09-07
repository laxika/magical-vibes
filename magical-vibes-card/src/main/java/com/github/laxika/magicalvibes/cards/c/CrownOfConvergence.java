package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesSharingColorWithTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfLibraryOnBottomEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "258")
public class CrownOfConvergence extends Card {

    public CrownOfConvergence() {
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesSharingColorWithTopCardEffect(1, 1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{W}",
                List.of(new PutTopCardsOfLibraryOnBottomEffect(1)),
                "{G}{W}: Put the top card of your library on the bottom of your library."
        ));
    }
}
