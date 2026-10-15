package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.TheGoldenGearColossus;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsDoubleFacedPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "13")
@CardRegistration(set = "LCC", collectorNumber = "31")
public class TetzinGnomeChampion extends Card {

    private static final MillControllerAndMayReturnMilledPermanentToHandEffect MILL_ARTIFACT =
            new MillControllerAndMayReturnMilledPermanentToHandEffect(3,
                    new CardTypePredicate(CardType.ARTIFACT));

    public TetzinGnomeChampion() {
        setBackFaceCard(new TheGoldenGearColossus());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, MILL_ARTIFACT);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsDoubleFacedPredicate())),
                        MILL_ARTIFACT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new ExileSelfCost(),
                        new CraftMaterialCost(6, CardType.ARTIFACT, false, false),
                        new ReturnSourceFromExileTransformedEffect()),
                "Craft with six artifacts {4} ({4}, Exile this artifact, Exile the six from among other "
                        + "permanents you control and/or cards from your graveyard: Return this card transformed "
                        + "under its owner's control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheGoldenGearColossus";
    }
}
