package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnColoredCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "53")
public class Demilich extends Card {

    public Demilich() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addEffect(EffectSlot.STATIC, new ReduceOwnColoredCastCostEffect(ManaColor.BLUE,
                new SpellsCastThisTurn(instantOrSorcery, CountScope.CONTROLLER)));
        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, ownGraveyard), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                                instantOrSorcery, ownGraveyard, false));
        addEffect(EffectSlot.SPELL, new ExileNCardsFromGraveyardCost(4, null, instantOrSorcery, true));
        addCastingOption(new GraveyardCast());
    }
}
