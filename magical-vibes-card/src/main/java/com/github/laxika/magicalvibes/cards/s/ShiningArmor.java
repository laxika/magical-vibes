package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "29")
public class ShiningArmor extends Card {

    private static final PermanentAllOfPredicate KNIGHT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)
    ));

    public ShiningArmor() {
        // When this Equipment enters, attach it to target Knight you control.
        target(new ControlledPermanentPredicateTargetFilter(
                KNIGHT_CREATURE,
                "Target must be a Knight you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect());

        // Equipped creature gets +0/+2 and has vigilance.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
