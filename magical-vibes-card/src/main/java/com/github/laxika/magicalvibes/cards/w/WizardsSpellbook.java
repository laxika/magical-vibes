package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "82")
public class WizardsSpellbook extends Card {

    public WizardsSpellbook() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        GraveyardSearchScope allGraveyards = GraveyardSearchScope.ALL_GRAVEYARDS;

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(instantOrSorcery, allGraveyards),
                        new RollD20Effect(
                                CopyCardsExiledWithSourceAndMayCastCopiesEffect.oneForNormalCost(),
                                CopyCardsExiledWithSourceAndMayCastCopiesEffect.oneForOneGeneric(),
                                CopyCardsExiledWithSourceAndMayCastCopiesEffect.allForFree())),
                "{T}: Exile target instant or sorcery card from a graveyard. Roll a d20.",
                new GraveyardCardPredicateTargetFilter(instantOrSorcery, allGraveyards),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
