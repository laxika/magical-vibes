package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "36")
@CardRegistration(set = "SOC", collectorNumber = "84")
public class RenegadeBull extends Card {

    public RenegadeBull() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new BoostSelfByCastSpellManaValueEffect(instantOrSorcery, false));

        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, ownGraveyard), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                                instantOrSorcery, ownGraveyard));
    }
}
