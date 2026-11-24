package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The "except ..." of a copy effect ("becomes a copy of it, except it has flying"; "It's still a land"; "except its
 * name is Lazav, the Multifarious, it's legendary in addition to its other types"; "except it's 0/0"): values the copy
 * has instead of or beside those it copied. They become part of the copy's copiable values (CR 707.9b), so a copy of
 * the copy has them too. A copy given its power and toughness doesn't copy a characteristic-defining ability that
 * defines them (CR 707.9d).
 *
 * <p>Build them from {@link #NONE} or a factory and chain the others: {@code CopyExceptions.named("Lazav, the
 * Multifarious").alsoLegendary()}.
 *
 * @param addedCardTypes   card types the copy has in addition to the copied ones ("it's still a land")
 * @param addedKeywords    keyword abilities the copy has in addition to the copied ones ("except it has flying")
 * @param name             the copy's name instead of the copied one ("except its name is ..."); {@code null} to copy it
 * @param addedSupertypes  supertypes the copy has in addition to the copied ones ("it's legendary in addition to its
 *                         other types")
 * @param power            the copy's power instead of the copied one ("except it's 0/0"); {@code null} to copy it
 * @param toughness        the copy's toughness instead of the copied one; {@code null} to copy it
 * @param cardTypes        the copy's card types instead of the copied ones ("he's a legendary Human Mercenary Villain
 *                         creature", which says no "in addition to its other types", CR 205.1a); empty to copy them
 * @param creatureTypes    the copy's creature types instead of the copied ones, with {@code cardTypes}; empty to copy
 *                         them
 * @param addedSubtypes    subtypes the copy has in addition to the copied ones ("it's a Human Villain creature in
 *                         addition to his other types"); a creature type only while the copy is a creature or kindred
 *                         (CR 205.3d)
 * @param addedColors      colors the copy has in addition to the copied ones ("it's black in addition to its other
 *                         colors")
 * @param addedAbilities   abilities the copy has in addition to the copied ones ("except it has 'Whenever this creature
 *                         deals combat damage to a player, ...'"; CR 707.9a)
 */
public record CopyExceptions(Set<CardType> addedCardTypes, Set<Keyword> addedKeywords, String name,
                             Set<CardSupertype> addedSupertypes, Integer power, Integer toughness,
                             Set<CardType> cardTypes, List<CardSubtype> creatureTypes,
                             List<CardSubtype> addedSubtypes, Set<CardColor> addedColors,
                             List<Ability> addedAbilities) {

    /** No exceptions: the copy has exactly the copied values. */
    public static final CopyExceptions NONE = new CopyExceptions(Set.of(), Set.of(), null, Set.of(), null, null,
            Set.of(), List.of(), List.of(), Set.of(), List.of());

    public CopyExceptions {
        addedCardTypes = Set.copyOf(addedCardTypes);
        addedKeywords = Set.copyOf(addedKeywords);
        addedSupertypes = Set.copyOf(addedSupertypes);
        cardTypes = Set.copyOf(cardTypes);
        creatureTypes = List.copyOf(creatureTypes);
        addedSubtypes = List.copyOf(addedSubtypes);
        addedColors = Set.copyOf(addedColors);
        addedAbilities = List.copyOf(addedAbilities);
        if ((power == null) != (toughness == null)) {
            throw new IllegalArgumentException("A copy is given both its power and its toughness");
        }
        if (!creatureTypes.isEmpty() && !cardTypes.contains(CardType.CREATURE)) {
            throw new IllegalArgumentException("Creature types are set with the creature card type");
        }
    }

    /** "Except it has [keywords]." */
    public static CopyExceptions withKeywords(Keyword... keywords) {
        return NONE.alsoWithKeywords(keywords);
    }

    /** "It's still a [type]": the copy keeps that card type in addition to the copied ones. */
    public static CopyExceptions stillA(CardType... types) {
        return NONE.alsoStillA(types);
    }

    /** "Except its name is [name]." */
    public static CopyExceptions named(String name) {
        return NONE.alsoNamed(name);
    }

    /** "Except it's [power]/[toughness]." */
    public static CopyExceptions withBasePowerToughness(int power, int toughness) {
        return NONE.alsoWithBasePowerToughness(power, toughness);
    }

    /** These exceptions, and the copy "has [keywords]". */
    public CopyExceptions alsoWithKeywords(Keyword... keywords) {
        Set<Keyword> all = EnumSet.noneOf(Keyword.class);
        all.addAll(addedKeywords);
        all.addAll(List.of(keywords));
        return new CopyExceptions(addedCardTypes, all, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, addedColors, addedAbilities);
    }

    /** These exceptions, and "its name is [name]". */
    public CopyExceptions alsoNamed(String name) {
        return new CopyExceptions(addedCardTypes, addedKeywords, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, addedColors, addedAbilities);
    }

    /** These exceptions, and "it's legendary in addition to its other types". */
    public CopyExceptions alsoLegendary() {
        Set<CardSupertype> all = EnumSet.noneOf(CardSupertype.class);
        all.addAll(addedSupertypes);
        all.add(CardSupertype.LEGENDARY);
        return new CopyExceptions(addedCardTypes, addedKeywords, name, all, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, addedColors, addedAbilities);
    }

    /** These exceptions, and "it's [power]/[toughness]". */
    public CopyExceptions alsoWithBasePowerToughness(int power, int toughness) {
        return new CopyExceptions(addedCardTypes, addedKeywords, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, addedColors, addedAbilities);
    }

    /**
     * These exceptions, and "it's a [creature types] creature" with no "in addition to its other types": the copy's
     * card types are only creature and its creature types only {@code creatureTypes} (CR 205.1a).
     */
    public CopyExceptions alsoACreatureOfTypes(CardSubtype... creatureTypes) {
        return new CopyExceptions(addedCardTypes, addedKeywords, name, addedSupertypes, power, toughness,
                Set.of(CardType.CREATURE), List.of(creatureTypes), addedSubtypes, addedColors, addedAbilities);
    }

    /** These exceptions, and "it's still a [type]" / "a [type] in addition to its other types". */
    public CopyExceptions alsoStillA(CardType... types) {
        Set<CardType> all = EnumSet.noneOf(CardType.class);
        all.addAll(addedCardTypes);
        all.addAll(List.of(types));
        return new CopyExceptions(all, addedKeywords, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, addedColors, addedAbilities);
    }

    /** These exceptions, and "it's a [subtypes] in addition to its other types". */
    public CopyExceptions alsoWithSubtypes(CardSubtype... subtypes) {
        List<CardSubtype> all = new ArrayList<>(addedSubtypes);
        for (CardSubtype subtype : subtypes) {
            if (!all.contains(subtype)) {
                all.add(subtype);
            }
        }
        return new CopyExceptions(addedCardTypes, addedKeywords, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, all, addedColors, addedAbilities);
    }

    /** These exceptions, and "it's [colors] in addition to its other colors". */
    public CopyExceptions alsoColored(CardColor... colors) {
        Set<CardColor> all = EnumSet.noneOf(CardColor.class);
        all.addAll(addedColors);
        all.addAll(List.of(colors));
        return new CopyExceptions(addedCardTypes, addedKeywords, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, all, addedAbilities);
    }

    /** "Except it has [ability]" for an ability that is not a keyword (CR 707.9a). */
    public static CopyExceptions withAbilities(Ability... abilities) {
        return NONE.alsoWithAbilities(abilities);
    }

    /** These exceptions, and "it has [ability]". */
    public CopyExceptions alsoWithAbilities(Ability... abilities) {
        List<Ability> all = new ArrayList<>(addedAbilities);
        all.addAll(List.of(abilities));
        return new CopyExceptions(addedCardTypes, addedKeywords, name, addedSupertypes, power, toughness, cardTypes,
                creatureTypes, addedSubtypes, addedColors, all);
    }
}
