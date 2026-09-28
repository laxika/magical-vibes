package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class GainActivatedAndTriggeredAbilitiesOfExiledCardsSelfEffectHandler
        implements StaticEffectHandlerBean {

    private static final Set<EffectSlot> NON_ABILITY_SLOTS = EnumSet.of(
            EffectSlot.ON_TAP,
            EffectSlot.SPELL,
            EffectSlot.STATIC,
            EffectSlot.MAY_SKIP_DRAW_STEP_DRAW,
            EffectSlot.GRAVEYARD_DRAW_REPLACEMENT,
            EffectSlot.MULLIGAN_ACTION,
            EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL);

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.sourceId());
        for (Card card : exiledCards) {
            for (ActivatedAbility ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(ability);
            }

            List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                accumulator.addActivatedAbility(new ActivatedAbility(
                        true, null, onTapEffects, "{T}: Add mana."));
            }

            for (EffectSlot slot : EffectSlot.values()) {
                if (NON_ABILITY_SLOTS.contains(slot)) continue;
                for (CardEffect triggeredEffect : card.getEffects(slot)) {
                    accumulator.addGrantedEffect(new GrantTriggeredAbilityEffect(
                            slot, triggeredEffect, GrantScope.SELF));
                }
            }
        }
    }
}
