package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PutSourcePermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndLockTargetPermanentWhileTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "47")
@CardRegistration(set = "LCI", collectorNumber = "360")
public class BraidedNet extends Card {

    public BraidedNet() {
        setBackFaceCard(new BraidedQuipu());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.NET, new Fixed(3)));

        PermanentPredicate anotherNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.NET),
                        new TapAndLockTargetPermanentWhileTappedEffect(anotherNonland)
                ),
                "{T}, Remove a net counter from Braided Net: Tap another target nonland permanent. "
                        + "Its activated abilities can't be activated for as long as it remains tapped.",
                new PermanentPredicateTargetFilter(anotherNonland,
                        "Target must be another nonland permanent")));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(), new ReturnSourceFromExileTransformedEffect()),
                "Craft with artifact {1}{U} ({1}{U}, Exile this artifact, Exile another artifact you control "
                        + "or an artifact card from your graveyard: Return this card transformed under its owner's "
                        + "control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "BraidedQuipu";
    }
}
