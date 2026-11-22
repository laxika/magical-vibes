package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * Grants flashback (CR 702.34a), or another ability that works from the graveyard ({@code ability}: harmonize,
 * jump-start, embalm), to a single targeted card in the controller's graveyard until end of turn. Its cost equals
 * the card's mana cost unless a fixed {@code flashbackCost} is supplied or {@code withoutPayingManaCost} is true.
 * The target must match one of the specified card types.
 * (e.g. Snapcaster Mage, Songcrafter Mage, Filigree Racer, Cursecloth Wrappings)
 */
public record GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes,
                                                        boolean withoutPayingManaCost,
                                                        String flashbackCost,
                                                        GrantedCardAbilityEffect.Ability ability) implements CardEffect {
    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes) {
        this(cardTypes, false, null);
    }

    /** The grant of {@code ability} at the card's mana cost. */
    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes, GrantedCardAbilityEffect.Ability ability) {
        this(cardTypes, false, null, ability);
    }

    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes, boolean withoutPayingManaCost,
                                                     String flashbackCost) {
        this(cardTypes, withoutPayingManaCost, flashbackCost, GrantedCardAbilityEffect.Ability.FLASHBACK);
    }

    /** The granted ability's name, for prompts and the log. */
    public String abilityName() {
        return switch (ability) {
            case FLASHBACK -> "flashback";
            case HARMONIZE -> "harmonize";
            case JUMP_START -> "jump-start";
            case EMBALM -> "embalm";
            case UNEARTH -> "unearth";
            case WARP -> "warp";
        };
    }

    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes, boolean withoutPayingManaCost) {
        this(cardTypes, withoutPayingManaCost, null);
    }

    public GrantFlashbackToTargetGraveyardCardEffect(Set<CardType> cardTypes, String flashbackCost) {
        this(cardTypes, false, flashbackCost);
    }

    @Override public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardAnyOfPredicate(cardTypes.stream().<CardPredicate>map(CardTypePredicate::new).toList()),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
