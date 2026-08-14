package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "128")
public class GuardianIdol extends Card {

    public GuardianIdol() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AnimatePermanentsEffect(
                        2, 2, List.of(CardSubtype.GOLEM), Set.of(), null, Set.of(CardType.ARTIFACT))),
                "{2}: This artifact becomes a 2/2 Golem artifact creature until end of turn."
        ));
    }
}
