package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "135")
public class DreadStatuary extends Card {

    public DreadStatuary() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new AnimatePermanentsEffect(
                        4, 2, List.of(CardSubtype.GOLEM), Set.of(), null, Set.of(CardType.ARTIFACT))),
                "{4}: This land becomes a 4/2 Golem artifact creature until end of turn. It's still a land."
        ));
    }
}
