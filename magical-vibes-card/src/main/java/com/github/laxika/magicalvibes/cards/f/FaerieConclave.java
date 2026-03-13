package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "351")
public class FaerieConclave extends Card {

    public FaerieConclave() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new AnimateLandEffect(2, 1, List.of(CardSubtype.FAERIE), Set.of(Keyword.FLYING), CardColor.BLUE)),
                "{1}{U}: Faerie Conclave becomes a 2/1 blue Faerie creature with flying until end of turn. It's still a land."
        ));
    }
}
