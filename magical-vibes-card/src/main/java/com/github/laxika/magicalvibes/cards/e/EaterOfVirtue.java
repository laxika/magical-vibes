package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "NEO", collectorNumber = "245")
public class EaterOfVirtue extends Card {

    public EaterOfVirtue() {
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES,
                new ExileTriggeringCreatureAndTrackWithSourceEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
