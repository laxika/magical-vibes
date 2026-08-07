package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

/**
 * Sachi, Daughter of Seshiro — {2}{G}{G} Legendary Creature — Snake Shaman 1/3
 *
 * Other Snake creatures you control get +0/+1.
 * Shamans you control have "{T}: Add {G}{G}."
 */
@CardRegistration(set = "CHK", collectorNumber = "238")
public class SachiDaughterOfSeshiro extends Card {

    public SachiDaughterOfSeshiro() {
        // "Other" — OWN_CREATURES excludes the source.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SNAKE)));

        // Sachi is a Shaman herself, so the grant includes her: ALL_OWN_CREATURES.
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null,
                        List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                        "{T}: Add {G}{G}."),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SHAMAN)));
    }
}
