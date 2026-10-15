package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.IfritWardenOfInferno;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "133")
@CardRegistration(set = "FIN", collectorNumber = "318")
@CardRegistration(set = "FIN", collectorNumber = "385")
@CardRegistration(set = "FIN", collectorNumber = "458")
@CardRegistration(set = "FIN", collectorNumber = "530")
public class CliveIfritsDominant extends Card {

    public CliveIfritsDominant() {
        setBackFaceCard(new IfritWardenOfInferno());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardOwnHandThenDrawEffect(
                        new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED)),
                "Discard your hand and draw cards equal to your devotion to red?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}{R}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{4}{R}{R}, {T}: Exile Clive, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "IfritWardenOfInferno";
    }
}
