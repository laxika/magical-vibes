package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "195")
public class JadeSeedstones extends Card {

    public JadeSeedstones() {
        setBackFaceCard(new JadeheartAttendant());

        target(TargetFilters.creatureYouControl(), 1, 3)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        DistributeCountersAmongTargetsEffect.chosenAmongTargetCreaturesEtb(
                                CounterType.PLUS_ONE_PLUS_ONE, 3,
                                new PermanentControlledBySourceControllerPredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(1, CardType.CREATURE, false, false),
                        new ReturnSourceFromExileTransformedEffect()),
                "Craft with creature {5}{G}{G} ({5}{G}{G}, Exile this artifact, Exile a creature you control "
                        + "or a creature card from your graveyard: Return this card transformed under its owner's "
                        + "control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "JadeheartAttendant";
    }
}
