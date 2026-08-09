package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsAsEntersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "108")
public class OverlaidTerrain extends Card {

    public OverlaidTerrain() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeAllPermanentsAsEntersEffect(
                new PermanentIsLandPredicate()));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect(2)),
                        "{T}: Add two mana of any one color."),
                GrantScope.OWN_LANDS));
    }
}
