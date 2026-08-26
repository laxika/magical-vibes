package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/** Back face of {@link DireFlail}. */
public class DireBlunderbuss extends Card {

    public DireBlunderbuss() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentNotPredicate(new PermanentNamedPredicate("Dire Blunderbuss")))),
                                new DealDamageToTargetCreatureEffect(new SourcePower()),
                                "an artifact other than Dire Blunderbuss"),
                        "Sacrifice an artifact other than Dire Blunderbuss?"),
                GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
