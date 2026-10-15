package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "119")
public class EnsouledScimitar extends Card {

    public EnsouledScimitar() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AnimatePermanentsEffect(1, 5, List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING))),
                "{3}: This artifact becomes a 1/5 Spirit artifact creature with flying until end of turn."
        ));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 5, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
