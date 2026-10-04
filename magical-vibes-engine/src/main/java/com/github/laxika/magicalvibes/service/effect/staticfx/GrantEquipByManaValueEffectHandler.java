package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEquipByManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GrantEquipByManaValueEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEquipByManaValueEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEquipByManaValueEffect) effect;
        Permanent target = context.target();

        // Grant equip ability to matching permanents
        if (support.matchesStaticFilter(context, target, grant.filter())) {
            int manaValue = target.getCard().getManaValue();
            accumulator.addGrantedEffect(new AttachedBoostEffect(
                    new Fixed(manaValue), new Fixed(0), GrantScope.EQUIPPED_CREATURE));
            String cost = "{" + manaValue + "}";
            accumulator.addActivatedAbility(new ActivatedAbility(
                    false,
                    cost,
                    List.of(new EquipEffect()),
                    "Equip " + cost,
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
}
