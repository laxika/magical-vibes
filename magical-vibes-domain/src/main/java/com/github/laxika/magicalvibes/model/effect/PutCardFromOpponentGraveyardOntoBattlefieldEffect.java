package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Puts a targeted card from an opponent's graveyard onto the battlefield under the controller's
 * control (tapped when {@code tapped=true}). The target must match {@link #filter}; when
 * {@link #requireManaValueEqualsX} is {@code true} its mana value must also equal the spell/ability's
 * X value, and after resolution that opponent mills X cards (Geth, Lord of the Vault). Ashen Powder
 * uses a plain creature filter with no mana-value constraint and no mill.
 */
public record PutCardFromOpponentGraveyardOntoBattlefieldEffect(
        boolean tapped, CardPredicate filter, boolean requireManaValueEqualsX) implements CardEffect {

    public PutCardFromOpponentGraveyardOntoBattlefieldEffect() {
        this(false);
    }

    public PutCardFromOpponentGraveyardOntoBattlefieldEffect(boolean tapped) {
        this(tapped,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE))),
                true);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.GRAVEYARD_CARD); }
}
