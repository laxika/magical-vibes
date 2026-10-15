package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainAbilitiesOfLastChosenExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "107")
public class KohTheFaceStealer extends Card {

    public KohTheFaceStealer() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentAndTrackWithSourceEffect());
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new MayEffect(new ExileTriggeringCreatureAndTrackWithSourceEffect(),
                        "Exile that creature with Koh?"));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(1), new ChooseCreatureCardExiledWithSourceEffect()),
                "Pay 1 life: Choose a creature card exiled with Koh."
        ));
        addEffect(EffectSlot.STATIC, new GainAbilitiesOfLastChosenExiledCardEffect());
    }
}
