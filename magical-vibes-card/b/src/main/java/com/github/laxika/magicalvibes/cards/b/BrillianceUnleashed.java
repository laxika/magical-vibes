package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentIfNotCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "141")
public class BrillianceUnleashed extends Card {

    public BrillianceUnleashed() {
        CardTypePredicate artifact = new CardTypePredicate(CardType.ARTIFACT);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Brilliance Unleashed deals 5 damage to target creature",
                        new DealDamageToTargetCreatureEffect(5), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target artifact card from your graveyard to the battlefield; if it isn't an artifact creature card, it becomes a 3/3 Robot artifact creature with flying",
                        List.of(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(artifact)
                                        .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                                        .targetGraveyard(true)
                                        .build(),
                                new AnimateReturnedPermanentIfNotCreatureEffect(
                                        new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.ROBOT),
                                                Set.of(Keyword.FLYING), null, Set.of(), GrantScope.TARGET,
                                                EffectDuration.PERMANENT))),
                        new GraveyardCardPredicateTargetFilter(artifact,
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        )));
    }
}
