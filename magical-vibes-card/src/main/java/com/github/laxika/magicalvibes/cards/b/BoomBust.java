package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "112")
public class BoomBust extends Card {

    public BoomBust() {
        TargetFilter landYouControl = TargetFilters.landYouControl();
        TargetFilter landYouDoNotControl = TargetFilters.landAnOpponentControls();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Boom — Destroy target land you control and target land you don't control",
                        List.<CardEffect>of(new DestroyTargetPermanentEffect(), new DestroyTargetPermanentEffect()),
                        List.of(landYouControl, landYouDoNotControl)
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Bust — Destroy all lands",
                        new DestroyAllPermanentsEffect(new PermanentIsLandPredicate())
                ).withManaCost("{5}{R}")
        )));
    }
}
