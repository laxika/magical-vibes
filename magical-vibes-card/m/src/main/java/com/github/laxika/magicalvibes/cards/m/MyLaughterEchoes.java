package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SchemeSetInMotionTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "344")
public class MyLaughterEchoes extends Card {

    private static final CardPredicate NON_ONGOING_SCHEME = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.SCHEME),
            new CardNotPredicate(new CardSupertypePredicate(CardSupertype.ONGOING))));

    public MyLaughterEchoes() {
        addEffect(EffectSlot.ON_CONTROLLER_SETS_SCHEME_IN_MOTION,
                new SchemeSetInMotionTriggerEffect(NON_ONGOING_SCHEME, List.of(
                        new MayEffect(
                                new AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect(),
                                "Abandon this scheme and set that scheme in motion again?"))));
    }
}
