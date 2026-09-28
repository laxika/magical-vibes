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
import com.github.laxika.magicalvibes.model.effect.SpecializeViconiaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

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
                        new SpecializeViconiaEffect(color)),
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
}
