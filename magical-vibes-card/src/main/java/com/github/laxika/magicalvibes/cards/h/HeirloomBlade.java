package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SLD", collectorNumber = "372")
public class HeirloomBlade extends Card {

    public HeirloomBlade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new MayEffect(
                new RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect(),
                "Reveal cards from the top of your library until you reveal a creature card that shares a creature type with it?"));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
