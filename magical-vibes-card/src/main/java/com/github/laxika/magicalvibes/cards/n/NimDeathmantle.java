package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "188")
public class NimDeathmantle extends Card {

    public NimDeathmantle() {
        // Equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(2, 2));
        // Equipped creature has intimidate
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.EQUIPPED_CREATURE));
        // Equipped creature is a black Zombie (replaces existing colors and creature subtypes per CR 205.1a)
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLACK, GrantScope.EQUIPPED_CREATURE, true));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.ZOMBIE, GrantScope.EQUIPPED_CREATURE, true));

        // Whenever a nontoken creature is put into your graveyard from the battlefield,
        // you may pay {4}. If you do, return that card to the battlefield and attach
        // Nim Deathmantle to it.
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new MayPayManaEffect("{4}",
                        new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(),
                        "Pay {4} to return this creature to the battlefield?"));

        // Equip {4}
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new EquipEffect()),
                "Equip {4}",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
