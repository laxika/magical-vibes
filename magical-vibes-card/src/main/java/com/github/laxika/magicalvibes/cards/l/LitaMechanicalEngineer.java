package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "4")
public class LitaMechanicalEngineer extends Card {

    public LitaMechanicalEngineer() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new UntapPermanentsEffect(
                        TapUntapScope.OTHER_CONTROLLED_CREATURES,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(new CreateTokenEffect(
                        CardType.ARTIFACT,
                        1,
                        "Zeppelin",
                        5,
                        5,
                        null,
                        null,
                        List.of(CardSubtype.VEHICLE),
                        Set.of(Keyword.FLYING),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(new ActivatedAbility(
                                false,
                                null,
                                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                                "Crew 3")),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )),
                "{3}{W}, {T}: Create a 5/5 colorless Vehicle artifact token named Zeppelin with flying and crew 3."
        ));
    }
}
