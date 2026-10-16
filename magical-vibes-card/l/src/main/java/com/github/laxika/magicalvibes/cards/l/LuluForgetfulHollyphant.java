package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostTriggeringAttackersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeLuluEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "3")
public class LuluForgetfulHollyphant extends Card {

    public LuluForgetfulHollyphant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardKeywordPredicate(Keyword.FLYING)))),
                        List.of(new PerpetuallyGrantKeywordsToTriggeringCardEffect(
                                Set.of(Keyword.FLYING)))));

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
                        new SpecializeLuluEffect(color)),
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static void addAttackTrigger(Card card, CardColor color) {
        PermanentPredicate otherFlyers = new PermanentAllOfPredicate(List.of(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        if (color == CardColor.GREEN) {
            card.addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                    new BoostTriggeringAttackersEffect(otherFlyers, 2, 2));
            return;
        }
        CardEffect effect = switch (color) {
            case WHITE -> new GainLifeEffect(new Scaled(new EventValue(), 2));
            case BLUE -> SequenceEffect.of(
                    new DrawCardEffect(new EventValue()),
                    new DiscardEffect(1, DiscardRecipient.CONTROLLER));
            case BLACK -> SequenceEffect.of(
                    new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT),
                    new GainLifeEffect(new EventValue()));
            case RED -> new CreateTokenEffect(CardType.CREATURE, new EventValue(), "Soldier", 1, 1,
                    CardColor.WHITE, null, List.of(CardSubtype.SOLDIER), Set.of(), Set.of(),
                    true, false, Map.of(), List.of(), false, false, false, 0, Set.of());
            default -> throw new IllegalStateException("Unsupported Lulu specialization color: " + color);
        };
        card.addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumMatchingAttackers(1, otherFlyers), effect));
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("3");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.ELEPHANT, CardSubtype.ANGEL));
        card.setKeywords(Set.of(Keyword.FLYING));
        card.setPower(2);
        card.setToughness(4);
    }

    public static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Lulu, Helpful Hollyphant");
                card.setManaCost("{2}{W}{W}");
                card.setColors(List.of(CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("Flying\nWhenever you attack with one or more other creatures with flying, "
                        + "you gain twice that much life.");
            }
            case BLUE -> {
                card.setName("Lulu, Curious Hollyphant");
                card.setManaCost("{2}{W}{U}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                card.setColor(CardColor.WHITE);
                card.setCardText("Flying\nWhenever you attack with one or more other creatures with flying, "
                        + "draw that many cards, then discard a card.");
            }
            case BLACK -> {
                card.setName("Lulu, Vengeful Hollyphant");
                card.setManaCost("{2}{W}{B}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColor(CardColor.WHITE);
                card.setCardText("Flying\nWhenever you attack with one or more other creatures with flying, "
                        + "each opponent loses that much life and you gain that much life.");
            }
            case RED -> {
                card.setName("Lulu, Inspiring Hollyphant");
                card.setManaCost("{2}{R}{W}");
                card.setColors(List.of(CardColor.RED, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("Flying\nWhenever you attack with one or more other creatures with flying, "
                        + "create that many 1/1 white Soldier creature tokens that are tapped and attacking.");
            }
            case GREEN -> {
                card.setName("Lulu, Wild Hollyphant");
                card.setManaCost("{2}{G}{W}");
                card.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("Flying\nWhenever you attack with one or more other creatures with flying, "
                        + "those creatures get +2/+2 until end of turn.");
            }
            default -> throw new IllegalStateException("Unsupported Lulu specialization color: " + color);
        }
    }
}
