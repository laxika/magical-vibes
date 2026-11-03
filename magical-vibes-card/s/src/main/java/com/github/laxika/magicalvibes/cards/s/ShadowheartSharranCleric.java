package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeShadowheartEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "10")
public class ShadowheartSharranCleric extends Card {

    public ShadowheartSharranCleric() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_PLAYER));

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeShadowheartEffect(color)),
                "Specialize {2} \u2014 " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED)
                .withActivationCondition(
                        new AnyOf(List.of(new ControllerLifeAtMost(13), new AnOpponentLifeAtMost(13))),
                        "Activate only if a player has 13 or less life"));
    }

    /**
     * The specialized card's "whenever you lose life during your turn" ability for {@code color}, or {@code null} for
     * black, which has none. "During your turn" is the ability's intervening "if".
     */
    public static TriggeredAbility specializedLifeLossAbility(CardColor color) {
        CardEffect effect = switch (color) {
            case WHITE -> new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                    List.of(CardSubtype.KNIGHT), Set.of(), Set.of());
            case BLUE -> new DrawCardEffect();
            case BLACK -> null;
            case RED -> new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT);
            case GREEN -> new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE);
        };
        return effect == null ? null : new TriggeredAbility(Triggers.youLoseLife(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), effect);
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("10");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.ELF, CardSubtype.CLERIC));
        card.setColor(CardColor.BLACK);
        card.setColors(List.of(CardColor.BLACK));
        card.setColorIdentity(List.of(CardColor.BLACK));
        card.setKeywords(Set.of(Keyword.DEATHTOUCH));
    }
}
