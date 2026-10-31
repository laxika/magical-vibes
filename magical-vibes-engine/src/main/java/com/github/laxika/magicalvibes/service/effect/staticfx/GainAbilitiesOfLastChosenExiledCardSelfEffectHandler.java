package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainAbilitiesOfLastChosenExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class GainAbilitiesOfLastChosenExiledCardSelfEffectHandler implements StaticEffectHandlerBean {

    private static final Set<EffectSlot> NON_ABILITY_SLOTS = EnumSet.of(
            EffectSlot.ON_TAP,
            EffectSlot.STATIC,
            EffectSlot.MAY_SKIP_DRAW_STEP_DRAW,
            EffectSlot.GRAVEYARD_DRAW_REPLACEMENT,
            EffectSlot.MULLIGAN_ACTION,
            EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL);

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainAbilitiesOfLastChosenExiledCardEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card chosenCard = context.source().getLastChosenExiledCard();
        if (chosenCard == null) return;
        if (context.gameData() != null && context.gameData().getCardsExiledByPermanent(context.source().getId())
                .stream().noneMatch(c -> c.getId().equals(chosenCard.getId()))) {
            return;
        }

        String link = "gained:" + chosenCard.getId();
        for (ActivatedAbility ability : chosenCard.getActivatedAbilities()) {
            accumulator.addActivatedAbility(ability.withEffects(ability.getEffects().stream()
                    .map(gainedEffect -> linkGainedEffect(gainedEffect, link)).toList()));
        }

        var onTapEffects = chosenCard.getEffects(EffectSlot.ON_TAP);
        if (!onTapEffects.isEmpty()) {
            accumulator.addActivatedAbility(new ActivatedAbility(
                    true, null, onTapEffects, "{T}: Add mana."));
        }

        for (EffectSlot slot : EffectSlot.values()) {
            if (NON_ABILITY_SLOTS.contains(slot)) continue;
            for (CardEffect triggeredEffect : chosenCard.getEffects(slot)) {
                accumulator.addGrantedEffect(new GrantTriggeredAbilityEffect(
                        slot, triggeredEffect, GrantScope.SELF));
            }
        }
    }

    /**
     * An exile ability gained from the chosen card tracks its cards under a link of its own, and the gained
     * ability that returns them looks only at that link, so neither touches cards exiled by the
     * gaining permanent's own abilities.
     */
    private static CardEffect linkGainedEffect(CardEffect effect, String link) {
        if (effect instanceof ExileTargetPermanentAndTrackWithSourceEffect) {
            return new ExileTargetPermanentAndTrackWithSourceEffect(link);
        }
        if (effect instanceof ReturnCardExiledWithSourceToBattlefieldEffect returnEffect) {
            return returnEffect.withAbilityLink(link);
        }
        return effect;
    }
}
