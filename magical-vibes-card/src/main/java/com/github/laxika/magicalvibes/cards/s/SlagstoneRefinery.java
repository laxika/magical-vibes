package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;

@CardRegistration(set = "BRO", collectorNumber = "243")
public class SlagstoneRefinery extends Card {

    public SlagstoneRefinery() {
        CreateTokenEffect powerstone = CreateTokenEffect.ofPowerstoneToken(new Fixed(1));
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, powerstone);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SelfExiledFromBattlefieldEffect(powerstone));
        addEffect(EffectSlot.ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD,
                powerstone);
    }
}
