package com.github.laxika.magicalvibes.cards.s;

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
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeSkanosEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "18")
public class SkanosDragonVassal extends Card {

    public SkanosDragonVassal() {
        addAttackAbility(this, new BoostTargetCreatureEffect(new SourcePower(), new Fixed(0)));

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addAttackAbility(Card card, CardEffect effect) {
        card.target(new PermanentPredicateTargetFilter(
                anotherAttackingCreature(), "Target must be another attacking creature"))
                .addEffect(EffectSlot.ON_ATTACK, effect);
    }

    public static CardEffect specializedAttackEffect(CardColor color) {
        CardEffect boost = new BoostTargetCreatureEffect(new SourcePower(), new Fixed(0));
        return switch (color) {
            case WHITE -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET), boost);
            case BLUE -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET), boost);
            case BLACK -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET), boost);
            case RED -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET), boost);
            case GREEN -> SequenceEffect.of(
                    new UntapPermanentsEffect(TapUntapScope.TARGET), boost);
        };
    }

    public static PermanentPredicate anotherAttackingCreature() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeSkanosEffect(color)),
                "Specialize {4} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("18");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.DRAGON, CardSubtype.RANGER));
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setColorIdentity(List.of(CardColor.GREEN));
        card.setKeywords(Set.of());
    }

    public static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Skanos, White Dragon Vassal");
                card.setManaCost("{4}{G}{W}");
                card.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setKeywords(Set.of(Keyword.LIFELINK));
                card.setCardText("Lifelink\nWhenever Skanos, White Dragon Vassal attacks, another target attacking creature gains lifelink and gets +X/+0 until end of turn, where X is Skanos's power.");
            }
            case BLUE -> {
                card.setName("Skanos, Blue Dragon Vassal");
                card.setManaCost("{4}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setKeywords(Set.of(Keyword.FLYING));
                card.setCardText("Flying\nWhenever Skanos, Blue Dragon Vassal attacks, another target attacking creature gains flying and gets +X/+0 until end of turn, where X is Skanos's power.");
            }
            case BLACK -> {
                card.setName("Skanos, Black Dragon Vassal");
                card.setManaCost("{4}{B}{G}");
                card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColor(CardColor.BLACK);
                card.setKeywords(Set.of(Keyword.MENACE));
                card.setPower(5);
                card.setToughness(5);
                card.setCardText("Menace\nWhenever Skanos, Black Dragon Vassal attacks, another target attacking creature gains menace and gets +X/+0 until end of turn, where X is Skanos's power.");
            }
            case RED -> {
                card.setName("Skanos, Red Dragon Vassal");
                card.setManaCost("{4}{R}{G}");
                card.setColors(List.of(CardColor.RED, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                card.setColor(CardColor.RED);
                card.setKeywords(Set.of(Keyword.FIRST_STRIKE));
                card.setPower(5);
                card.setToughness(5);
                card.setCardText("First strike\nWhenever Skanos, Red Dragon Vassal attacks, another target attacking creature gains first strike and gets +X/+0 until end of turn, where X is Skanos's power.");
            }
            case GREEN -> {
                card.setName("Skanos, Green Dragon Vassal");
                card.setManaCost("{4}{G}{G}");
                card.setKeywords(Set.of(Keyword.VIGILANCE));
                card.setPower(6);
                card.setToughness(6);
                card.setCardText("Vigilance\nWhenever Skanos, Green Dragon Vassal attacks, untap another target attacking creature. It gets +X/+0 until end of turn, where X is Skanos's power.");
            }
        }
    }
}
