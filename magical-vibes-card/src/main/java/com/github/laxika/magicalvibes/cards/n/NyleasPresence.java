package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "169")
public class NyleasPresence extends Card {

    public NyleasPresence() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

        List.of(
                new BasicLandGrant(CardSubtype.PLAINS, ManaColor.WHITE),
                new BasicLandGrant(CardSubtype.ISLAND, ManaColor.BLUE),
                new BasicLandGrant(CardSubtype.SWAMP, ManaColor.BLACK),
                new BasicLandGrant(CardSubtype.MOUNTAIN, ManaColor.RED),
                new BasicLandGrant(CardSubtype.FOREST, ManaColor.GREEN)
        ).forEach(grant -> {
            addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(grant.subtype(), GrantScope.ENCHANTED_PERMANENT));
            addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                    ManaAbilities.tapFor(grant.color()), GrantScope.ENCHANTED_PERMANENT));
        });
    }

    private record BasicLandGrant(CardSubtype subtype, ManaColor color) {
    }
}
