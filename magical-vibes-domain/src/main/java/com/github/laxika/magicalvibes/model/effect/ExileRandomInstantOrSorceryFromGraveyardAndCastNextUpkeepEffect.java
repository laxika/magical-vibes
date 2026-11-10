package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Exiles an instant or sorcery card chosen at random from the controller's graveyard and offers it
 * for a free cast at the beginning of the controller's next upkeep. If cast, the spell is exiled
 * instead of being put into a graveyard.
 *
 * <p>The card is the trigger's target, "target instant or sorcery card from your graveyard chosen at random": the
 * effect declares it ({@link #targetSpec()}) and that it is chosen at random ({@link #targetChosenAtRandom()}), so the
 * collector picks it as the ability is put onto the stack; it is the stack entry's {@code targetId} (in the
 * graveyard), and the ability does nothing if the card has left the graveyard (CR 608.2b).</p>
 */
public record ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardAnyOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }

    @Override
    public boolean targetChosenAtRandom() {
        return true;
    }
}
