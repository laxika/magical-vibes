package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPG", collectorNumber = "25")
public class CrashingFootfalls extends Card {

    public CrashingFootfalls() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Rhino", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.RHINO), Set.of(Keyword.TRAMPLE), Set.of()));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(),
                "Suspend 4\u2014{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
