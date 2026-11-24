package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "4")
public class RasaadMonkOfSelNe extends Card {

    public RasaadMonkOfSelNe() {
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new SourceIsOnBattlefield(), new ExileTargetPermanentAndTrackWithSourceEffect()));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnAllCardsExiledWithSourceEffect());

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new DiscardCardTypeCost(discardFilter,
                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()));
        effects.add(new SpecializeEffect(color, RasaadMonkOfSelNe::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                effects,
                "Specialize {5} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return switch (color) {
            case WHITE -> new PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect();
            case BLUE -> new CreateTokenEffect(2, "Faerie Dragon", 1, 1, CardColor.BLUE,
                    List.of(CardSubtype.FAERIE, CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of());
            case BLACK -> new CreateTokenEffect("Skeleton", 4, 1, CardColor.BLACK,
                    List.of(CardSubtype.SKELETON), Set.of(Keyword.MENACE), Set.of());
            case RED -> CreateTokenEffect.whiteSoldier(3);
            case GREEN -> new CreateTokenEffect(2, "Boar", 2, 2, CardColor.GREEN,
                    List.of(CardSubtype.BOAR), Set.of(), Set.of());
        };
    }

    /** Writes Rasaad's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setBaseFaceCharacteristics(face);
        face.addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ReturnAllCardsExiledWithSourceEffect());

        CardEffect specializedTrigger = specializedTrigger(color);
        switch (color) {
            case WHITE -> {
                face.setName("Rasaad, Radiant Monk");
                face.setManaCost("{2}{W}{W}");
                face.setCardText("When this creature specializes, target creature card exiled with this creature perpetually loses all abilities and has base power and toughness 1/1.");
                face.setPower(4);
                face.setToughness(4);
                face.addTrigger(Triggers.enters(ObjectRelation.THIS), specializedTrigger);
            }
            case BLUE -> {
                face.setName("Rasaad, Dragon Monk");
                face.setManaCost("{2}{W}{U}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setCardText("When Rasaad, Dragon Monk dies, create two 1/1 blue Faerie Dragon creature tokens with flying.");
                face.setPower(4);
                face.setToughness(4);
                face.addTrigger(Triggers.dies(ObjectRelation.THIS), specializedTrigger);
            }
            case BLACK -> {
                face.setName("Rasaad, Shadow Monk");
                face.setManaCost("{2}{W}{B}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setCardText("When Rasaad, Shadow Monk dies, create a 4/1 black Skeleton creature token with menace.");
                face.setPower(4);
                face.setToughness(4);
                face.addTrigger(Triggers.dies(ObjectRelation.THIS), specializedTrigger);
            }
            case RED -> {
                face.setName("Rasaad, Warrior Monk");
                face.setManaCost("{2}{R}{W}");
                face.setColors(List.of(CardColor.RED, CardColor.WHITE));
                face.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                face.setColor(CardColor.RED);
                face.setCardText("When Rasaad, Warrior Monk dies, create three 1/1 white Soldier creature tokens.");
                face.setPower(4);
                face.setToughness(4);
                face.addTrigger(Triggers.dies(ObjectRelation.THIS), specializedTrigger);
            }
            case GREEN -> {
                face.setName("Rasaad, Sylvan Monk");
                face.setManaCost("{2}{G}{W}");
                face.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                face.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                face.setColor(CardColor.GREEN);
                face.setCardText("When Rasaad, Sylvan Monk dies, create two 2/2 green Boar creature tokens.");
                face.setPower(4);
                face.setToughness(4);
                face.addTrigger(Triggers.dies(ObjectRelation.THIS), specializedTrigger);
            }
            default -> throw new IllegalStateException("Unsupported Rasaad specialization color: "
                    + color);
        }

        return color == CardColor.WHITE ? List.of(specializedTrigger) : List.of();
    }

    private static void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("4");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.MONK));
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE));
        card.setColorIdentity(List.of(CardColor.WHITE));
        card.setKeywords(Set.of());
    }
}
