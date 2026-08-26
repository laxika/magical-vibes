package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "250")
public class YunaHopeOfSpira extends Card {

    private static final Set<Keyword> TURN_KEYWORDS = Set.of(Keyword.TRAMPLE, Keyword.LIFELINK);

    public YunaHopeOfSpira() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(TURN_KEYWORDS, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(TURN_KEYWORDS, GrantScope.ALL_OWN_CREATURES,
                        new PermanentIsEnchantmentPredicate())));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(2), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(2), GrantScope.ALL_OWN_CREATURES,
                        new PermanentIsEnchantmentPredicate())));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                .targetGraveyard(true)
                .upTo(true)
                .enterWithCounter(CounterType.FINALITY)
                .enterWithCounterCount(1)
                .build());
    }
}
