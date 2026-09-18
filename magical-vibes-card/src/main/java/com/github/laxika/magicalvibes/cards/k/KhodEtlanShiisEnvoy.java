package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "24")
public class KhodEtlanShiisEnvoy extends Card {

    public KhodEtlanShiisEnvoy() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.ISLAND, GrantScope.ALL_LANDS));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.BLUE), GrantScope.ALL_LANDS));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.HOMARID,
                        CardSubtype.CAMARID,
                        CardSubtype.CEPHALID,
                        CardSubtype.NAUTILID,
                        CardSubtype.MERFOLK))));
    }
}
