package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesColorWithEquippedCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithEquippedCreaturePredicate;

/**
 * Both anthems are relative to the equipped creature and so cover it too — a creature always
 * shares its own colors and creature types, giving it +2/+2 when it has both.
 */
@CardRegistration(set = "CHK", collectorNumber = "259")
public class KondasBanner extends Card {

    public KondasBanner() {
        setAttachRestriction(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.ALL_CREATURES, new PermanentSharesColorWithEquippedCreaturePredicate()));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.ALL_CREATURES, new PermanentSharesCreatureTypeWithEquippedCreaturePredicate()));
        addActivatedAbility(new EquipActivatedAbility(
                "{2}",
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                "Konda's Banner can be attached only to a legendary creature"));
    }
}
