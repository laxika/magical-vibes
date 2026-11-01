package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "372")
@CardRegistration(set = "LTC", collectorNumber = "279")
@CardRegistration(set = "AFC", collectorNumber = "208")
@CardRegistration(set = "LCC", collectorNumber = "303")
@CardRegistration(set = "C20", collectorNumber = "242")
@CardRegistration(set = "C17", collectorNumber = "52")
@CardRegistration(set = "ZNC", collectorNumber = "113")
public class HeirloomBlade extends Card {

    public HeirloomBlade() {
        addStaticEffect(new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED), new MayEffect(
                new RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect(),
                "Reveal cards from the top of your library until you reveal a creature card that shares a creature type with it?"));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
