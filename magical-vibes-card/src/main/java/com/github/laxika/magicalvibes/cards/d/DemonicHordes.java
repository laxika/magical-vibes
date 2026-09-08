package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesPermanentToSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "104")
public class DemonicHordes extends Card {

    public DemonicHordes() {
        // {T}: Destroy target land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect()),
                "{T}: Destroy target land.",
                TargetFilters.land()
        ));

        // At the beginning of your upkeep, unless you pay {B}{B}{B}, tap this creature and
        // sacrifice a land of an opponent's choice.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PayManaCost("{B}{B}{B}"),
                List.of(
                        new TapPermanentsEffect(TapUntapScope.SELF),
                        new OpponentChoosesPermanentToSacrificeEffect(new PermanentIsLandPredicate())
                ),
                true));
    }
}
