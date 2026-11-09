package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "151")
@CardRegistration(set = "SLD", collectorNumber = "79")
@CardRegistration(set = "PIO", collectorNumber = "311")
public class KeranosGodOfStorms extends Card {

    public KeranosGodOfStorms() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.BLUE, ManaColor.RED), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        // Reveal the first card you draw on each of your turns. Whenever you reveal a land card this way, draw a
        // card. Whenever you reveal a nonland card this way, Keranos deals 3 damage to any target.
        addStaticEffect(RevealDrawnCardsEffect.firstOnEachOfYourTurns());
        addTrigger(Triggers.youRevealCardThisWay().withFilter(new CardTypePredicate(CardType.LAND)),
                new DrawCardEffect(1));
        addTrigger(Triggers.youRevealCardThisWay().withFilter(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                new DealDamageToAnyTargetEffect(3));
    }
}
