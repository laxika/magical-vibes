package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "OHOP", collectorNumber = "22")
public class Llanowar extends Card {

    public Llanowar() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null,
                        List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                        "{T}: Add {G}{G}."),
                GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED));
    }
}
