package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "245")
public class EaterOfVirtue extends Card {

    public EaterOfVirtue() {
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED),
                new ExileTriggeringCreatureAndTrackWithSourceEffect());
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
