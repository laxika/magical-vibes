package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2401")
public class KiboUktabiPrince extends Card {

    public KiboUktabiPrince() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EachPlayerCreatesTokenEffect(CreateTokenEffect.ofArtifactToken(
                        1,
                        "Banana",
                        List.of(),
                        List.of(new ActivatedAbility(
                                true,
                                null,
                                List.of(
                                        new SacrificeSelfCost(),
                                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN)),
                                        new GainLifeEffect(2)
                                ),
                                "{T}, Sacrifice this token: Add {R} or {G}. You gain 2 life."
                        ))))),
                "{T}: Each player creates a Banana token."
        ));

        addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.APE, CardSubtype.MONKEY))
                        )),
                        EachPermanentScope.ALL_PLAYERS));

        addEffect(EffectSlot.ON_ATTACK,
                new SacrificePermanentsEffect(
                        1,
                        new PermanentIsArtifactPredicate(),
                        SacrificeRecipient.DEFENDING_PLAYER));
    }
}
