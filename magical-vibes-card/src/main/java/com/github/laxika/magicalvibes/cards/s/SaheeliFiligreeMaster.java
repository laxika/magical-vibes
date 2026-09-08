package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "219")
public class SaheeliFiligreeMaster extends Card {

    public SaheeliFiligreeMaster() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new ScryEffect(1),
                        new MayPayTapPermanentsEffect(
                                new TapMultiplePermanentsCost(1, new PermanentIsArtifactPredicate()),
                                new DrawCardEffect(1),
                                "Tap an untapped artifact you control?")),
                "+1: Scry 1. You may tap an untapped artifact you control. If you do, draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 2, "Thopter", 1, 1, null, Set.of(),
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT),
                        false, false, Map.of(), List.of(), false, false, false, 0, Set.of(Keyword.HASTE))),
                "\u22122: Create two 1/1 colorless Thopter artifact creature tokens with flying. They gain haste until end of turn."
        ));

        PermanentAllOfPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, artifactCreature),
                                new ReduceCastCostForMatchingSpellsEffect(
                                        new CardTypePredicate(CardType.ARTIFACT), 1, CostModificationScope.SELF)),
                        "Artifact creatures you control get +1/+1 and artifact spells you cast cost {1} less to cast.")),
                "\u22124: You get an emblem with \"Artifact creatures you control get +1/+1\" and \"Artifact spells you cast cost {1} less to cast.\""
        ));
    }
}
