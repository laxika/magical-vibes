package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
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
                DealDamageEffect.to(Subjects.eachPlayer(), 1));

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
                        new SpecializeEffect(color, ShadowheartSharranCleric::specializedFace)),
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
     * black, which has none. "During your turn" is the ability's event condition.
     */
    public static TriggeredAbility specializedLifeLossAbility(CardColor color) {
        CardEffect effect = switch (color) {
            case WHITE -> new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                    List.of(CardSubtype.KNIGHT), Set.of(), Set.of());
            case BLUE -> new DrawCardEffect();
            case BLACK -> null;
            case RED -> DealDamageEffect.to(Subjects.eachOpponent(), new EventValue());
            case GREEN -> PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE);
        };
        return effect == null ? null : new TriggeredAbility(Triggers.youLoseLife(),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()), effect);
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

    /** Writes Shadowheart's specialized face for {@code color}; it has no "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        face.setPower(4);
        face.setToughness(4);

        switch (color) {
            case WHITE -> {
                face.setName("Shadowheart, Cleric of Order");
                face.setManaCost("{1}{W}{B}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setColor(CardColor.WHITE);
                face.setCardText("Deathtouch\nAt the beginning of your end step, Shadowheart, Cleric of Order deals 1 damage to each player.\nWhenever you lose life during your turn, create a 2/2 white Knight creature token.");
            }
            case BLUE -> {
                face.setName("Shadowheart, Cleric of Trickery");
                face.setManaCost("{1}{U}{B}");
                face.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColor(CardColor.BLUE);
                face.setCardText("Deathtouch\nAt the beginning of your end step, Shadowheart, Cleric of Trickery deals 1 damage to each player.\nWhenever you lose life during your turn, draw a card.");
            }
            case BLACK -> {
                face.setName("Shadowheart, Cleric of Graves");
                face.setManaCost("{1}{B}{B}");
                face.setKeywords(Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK));
                face.setCardText("Deathtouch, lifelink\nAt the beginning of your end step, Shadowheart, Cleric of Graves deals 1 damage to each player.");
            }
            case RED -> {
                face.setName("Shadowheart, Cleric of War");
                face.setManaCost("{1}{B}{R}");
                face.setColors(List.of(CardColor.BLACK, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                face.setColor(CardColor.RED);
                face.setCardText("Deathtouch\nAt the beginning of your end step, Shadowheart, Cleric of War deals 1 damage to each player.\nWhenever you lose life during your turn, Shadowheart deals that much damage to each opponent.");
            }
            case GREEN -> {
                face.setName("Shadowheart, Cleric of Twilight");
                face.setManaCost("{1}{B}{G}");
                face.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                face.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                face.setColor(CardColor.GREEN);
                face.setCardText("Deathtouch\nShadowheart, Cleric of Twilight can't be blocked by creatures with power 2 or less.\nAt the beginning of your end step, Shadowheart deals 1 damage to each player.\nWhenever you lose life during your turn, put a +1/+1 counter on Shadowheart.");
                face.addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentPowerAtMostPredicate(2)));
            }
            default -> throw new IllegalStateException("Unsupported Shadowheart specialization color: "
                    + color);
        }

        face.addTrigger(Triggers.endStep(ActivePlayer.YOU),
                DealDamageEffect.to(Subjects.eachPlayer(), 1));
        TriggeredAbility lifeLossAbility = specializedLifeLossAbility(color);
        if (lifeLossAbility != null) {
            face.addTriggeredAbility(lifeLossAbility);
        }
        return List.of();
    }
}
