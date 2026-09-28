package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.l.LukaminaMoonDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeLukaminaEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UnspecializeLukaminaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Applies Lukamina, Moon Druid's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeLukaminaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeLukaminaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeLukaminaEffect specialize = (SpecializeLukaminaEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Lukamina, Moon Druid".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        LukaminaMoonDruid.setBaseFaceCharacteristics(specialized);
        LukaminaMoonDruid.setFaceCharacteristics(specialized, specialize.color());

        List<CardEffect> specializationTrigger = List.of();
        switch (specialize.color()) {
            case WHITE -> {
                // Hawk Form has only its printed keywords.
            }
            case BLUE -> {
                CardEffect tapEffect = new TapPermanentsEffect(TapUntapScope.TARGET);
                CardEffect lockEffect = DoesntUntapEffect.targetWhileSourceOnBattlefield();
                specialized.target(TargetFilters.nonlandPermanentAnOpponentControls())
                        .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, tapEffect)
                        .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                                lockEffect);
                specializationTrigger = List.of(tapEffect, lockEffect);
            }
            case BLACK -> specialized.addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
            case RED -> {
                CardEffect wolfTokenEffect = wolfTokenEffect();
                specialized.addEffect(EffectSlot.ON_ATTACK, wolfTokenEffect);
                specializationTrigger = List.of(wolfTokenEffect);
            }
            case GREEN -> specialized.addEffect(EffectSlot.STATIC,
                    new StaticBoostEffect(1, 1, Set.of(Keyword.TRAMPLE), GrantScope.OWN_CREATURES));
            default -> throw new IllegalStateException("Unsupported Lukamina specialization color: "
                    + specialize.color());
        }
        specialized.addEffect(EffectSlot.ON_DEATH, new UnspecializeLukaminaEffect());

        source.exchangeCard(specialized);
        if (!specializationTrigger.isEmpty()) {
            if (specialize.color() == CardColor.BLUE) {
                queueTargetedSpecializationTrigger(gameData, entry, source, specialized, specializationTrigger);
            } else {
                queueSpecializationTrigger(gameData, entry, specialized, specializationTrigger);
            }
        }
    }

    private void queueSpecializationTrigger(GameData gameData, StackEntry sourceEntry,
                                             Card sourceCard, List<CardEffect> effects) {
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard,
                sourceEntry.getControllerId(), sourceCard.getName() + "'s ability", effects, 0,
                sourceEntry.getSourcePermanentId());
        trigger.setNonTargeting(effects.stream().allMatch(effect -> effect.targetSpec() == TargetSpec.NONE));
        gameData.enqueueTrigger(trigger);
    }

    private void queueTargetedSpecializationTrigger(GameData gameData, StackEntry sourceEntry,
                                                     Permanent source, Card sourceCard, List<CardEffect> effects) {
        gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                sourceCard, sourceEntry.getControllerId(), effects, "specializes",
                sourceEntry.getSourcePermanentId(), new Permanent(source)));
    }

    private static CreateTokenEffect wolfTokenEffect() {
        return new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                List.of(com.github.laxika.magicalvibes.model.CardSubtype.WOLF), Set.of(), Set.of());
    }
}
