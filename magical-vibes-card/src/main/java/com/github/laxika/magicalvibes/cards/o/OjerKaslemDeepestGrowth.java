package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfCultivation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;

@CardRegistration(set = "LCI", collectorNumber = "204")
public class OjerKaslemDeepestGrowth extends Card {

    public OjerKaslemDeepestGrowth() {
        setBackFaceCard(new TempleOfCultivation());

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect
                        .creatureAndLandToBattlefieldRestOnBottomRandom(new EventValue()));
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect(true, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "TempleOfCultivation";
    }
}
