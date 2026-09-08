package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.WrappedGraveyardStaticEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "143")
public class RiftstonePortal extends Card {

    public RiftstonePortal() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addEffect(EffectSlot.STATIC, new WrappedGraveyardStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.WHITE))),
                        "{T}: Add {G} or {W}."
                ),
                GrantScope.OWN_LANDS
        )));
    }
}
