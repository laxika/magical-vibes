package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetExiledCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ExiledCardPredicateTargetFilter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "11")
public class ViconiaNightsingersDisciple extends Card {

    public ViconiaNightsingersDisciple() {
        addExileAbility(this);

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addExileAbility(Card card) {
        card.addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true)),
                "{1}: Exile target card from a graveyard."));
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
                        new SpecializeEffect(color, ViconiaNightsingersDisciple::specializedFace)),
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static List<ConjureDuplicateOfTargetExiledCardIntoHandEffect> specializedTriggers(CardColor color) {
        CardPredicate creature = new CardTypePredicate(CardType.CREATURE);
        return switch (color) {
            case WHITE -> List.of(new ConjureDuplicateOfTargetExiledCardIntoHandEffect(
                    creature, 0, 0, Set.of(), true, false, 3));
            case BLUE -> List.of(
                    new ConjureDuplicateOfTargetExiledCardIntoHandEffect(
                            creature, 0, 0, Set.of(), true, false, -1),
                    new ConjureDuplicateOfTargetExiledCardIntoHandEffect(
                            new CardAnyOfPredicate(List.of(
                                    new CardTypePredicate(CardType.INSTANT),
                                    new CardTypePredicate(CardType.SORCERY))),
                            0, 0, Set.of(), true, false, -1));
            case BLACK -> List.of(new ConjureDuplicateOfTargetExiledCardIntoHandEffect(
                    creature, 0, 0, Set.of(), true, true, -1));
            case RED -> List.of(new ConjureDuplicateOfTargetExiledCardIntoHandEffect(
                    creature, 1, 0, Set.of(Keyword.HASTE), true, false, -1));
            case GREEN -> List.of(new ConjureDuplicateOfTargetExiledCardIntoHandEffect(
                    creature, 2, 2, Set.of(), true, false, -1));
        };
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("11");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(com.github.laxika.magicalvibes.model.CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.ELF, CardSubtype.CLERIC));
        card.setColor(CardColor.BLACK);
        card.setColors(List.of(CardColor.BLACK));
        card.setColorIdentity(List.of(CardColor.BLACK));
        card.setKeywords(Set.of());
    }

    /** Writes Viconia's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        addExileAbility(face);
        face.setPower(3);
        face.setToughness(4);
        setFaceCharacteristics(face, color);
        List<ConjureDuplicateOfTargetExiledCardIntoHandEffect> triggers = specializedTriggers(color);
        installSpecializedTargets(face, triggers);
        return new ArrayList<>(triggers);
    }

    private static void installSpecializedTargets(Card specialized,
                                           List<ConjureDuplicateOfTargetExiledCardIntoHandEffect> triggers) {
        int minimumTargets = triggers.size() == 2 ? 0 : 1;
        for (ConjureDuplicateOfTargetExiledCardIntoHandEffect trigger : triggers) {
            int targetIndex = specialized.target(new ExiledCardPredicateTargetFilter(
                            trigger.filter(), "Target must be a matching card exiled with Viconia."),
                            minimumTargets, 1)
                    .getIndex();
            specialized.registerEffectTargetIndex(trigger, targetIndex);
        }
    }

    private static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Viconia, Disciple of Rebirth");
                card.setManaCost("{1}{W}{B}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColor(CardColor.WHITE);
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gains \"You may "
                        + "spend mana as though it were mana of any color to cast this spell.\" If it has mana "
                        + "value 3 or less, you may put it onto the battlefield.\n"
                        + "{1}: Exile target card from a graveyard.");
            }
            case BLUE -> {
                card.setName("Viconia, Disciple of Arcana");
                card.setManaCost("{1}{U}{B}");
                card.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColor(CardColor.BLUE);
                card.setCardText("When this creature specializes, choose up to one target creature card and up "
                        + "to one target instant or sorcery card from among cards exiled with this creature. "
                        + "Conjure a duplicate of each of those cards into your hand. The duplicates perpetually "
                        + "gain \"You may spend mana as though it were mana of any color to cast this spell.\"\n"
                        + "{1}: Exile target card from a graveyard.");
            }
            case BLACK -> {
                card.setName("Viconia, Disciple of Blood");
                card.setManaCost("{1}{B}{B}");
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gains \"You may "
                        + "spend mana as though it were mana of any color to cast this spell\" and \"When this "
                        + "creature enters, each opponent loses 2 life and you gain 2 life.\"\n"
                        + "{1}: Exile target card from a graveyard.");
            }
            case RED -> {
                card.setName("Viconia, Disciple of Violence");
                card.setManaCost("{1}{B}{R}");
                card.setColors(List.of(CardColor.BLACK, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                card.setColor(CardColor.RED);
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gets +1/+0 and "
                        + "gains haste and \"You may spend mana as though it were mana of any color to cast this "
                        + "spell.\"\n{1}: Exile target card from a graveyard.");
            }
            case GREEN -> {
                card.setName("Viconia, Disciple of Strength");
                card.setManaCost("{1}{B}{G}");
                card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setCardText("When this creature specializes, conjure a duplicate of target creature card "
                        + "exiled with this creature into your hand. The duplicate perpetually gets +2/+2 and "
                        + "gains \"You may spend mana as though it were mana of any color to cast this spell.\"\n"
                        + "{1}: Exile target card from a graveyard.");
            }
        }
    }
}
