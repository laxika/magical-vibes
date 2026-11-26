package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Objects;

/**
 * Static effect: "each [filter] card in your graveyard has [ability]". The ability is the same for every card
 * (Sedris, the Traitor King's unearth {2}{B}) or a keyword ability whose cost is that card's mana cost or mana
 * value (Naktamun, Wire Surgeons, Sliver Gravemother; scavenge for its mana cost, Varolz, the Scar-Striped, CR 702.97a). The engine judges {@code filter} with the card's current
 * characteristics.
 *
 * @param filter     the cards that have the ability
 * @param ability    the ability every such card has, or {@code null} for one whose cost depends on the card
 * @param perCard    the keyword ability whose cost depends on the card, or {@code null}
 */
public record GrantGraveyardAbilityToCardsEffect(CardPredicate filter, ActivatedAbility ability, PerCard perCard)
        implements GraveyardAbilityGrantingEffect {

    /** A keyword ability whose cost is read from the card that has it. */
    public enum PerCard {
        EMBALM_FOR_MANA_COST, UNEARTH_FOR_MANA_COST, SCAVENGE_FOR_MANA_COST, ENCORE_FOR_MANA_COST, ENCORE_FOR_MANA_VALUE
    }

    public GrantGraveyardAbilityToCardsEffect {
        Objects.requireNonNull(filter, "filter");
        if ((ability == null) == (perCard == null)) {
            throw new IllegalArgumentException("Grants either one ability or one whose cost depends on the card");
        }
    }

    public GrantGraveyardAbilityToCardsEffect(CardPredicate filter, ActivatedAbility ability) {
        this(filter, ability, null);
    }

    public GrantGraveyardAbilityToCardsEffect(CardPredicate filter, PerCard perCard) {
        this(filter, null, perCard);
    }

    @Override
    public CardPredicate cardFilter() {
        return filter;
    }

    @Override
    public ActivatedAbility grantedGraveyardAbilityFor(Card card) {
        if (ability != null) {
            return ability;
        }
        if (perCard == PerCard.ENCORE_FOR_MANA_VALUE) {
            return encore("{" + card.getManaValue() + "}");
        }
        // A card with no mana cost has no cost to pay.
        String cost = card.getManaCost();
        if (cost == null || cost.isBlank()) {
            return null;
        }
        return switch (perCard) {
            case EMBALM_FOR_MANA_COST -> Card.embalmAbility(cost);
            case UNEARTH_FOR_MANA_COST -> Card.unearthAbility(cost);
            case SCAVENGE_FOR_MANA_COST -> Card.scavengeAbility(cost);
            case ENCORE_FOR_MANA_COST, ENCORE_FOR_MANA_VALUE -> encore(cost);
        };
    }

    private static ActivatedAbility encore(String cost) {
        return new ActivatedAbility(false, cost,
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore " + cost,
                ActivationTimingRestriction.SORCERY_SPEED);
    }
}
