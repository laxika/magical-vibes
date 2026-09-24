package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "235")
public class SojournersCompanion extends Card {

    public SojournersCompanion() {
        // Affinity for artifacts.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));

        // Artifact landcycling {2} ({2}, Discard this card: Search your library for an artifact land
        // card, reveal it, put it into your hand, then shuffle.) — discard cost is intrinsic.
        CardPredicate artifactLand = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.LAND)));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(artifactLand)),
                "Artifact landcycling {2} ({2}, Discard this card: Search your library for an artifact "
                        + "land card, reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
