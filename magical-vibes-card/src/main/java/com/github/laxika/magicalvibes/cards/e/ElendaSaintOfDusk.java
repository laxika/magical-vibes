package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "119")
public class ElendaSaintOfDusk extends Card {

    public ElendaSaintOfDusk() {
        removeKeyword(Keyword.HEXPROOF);
        addEffect(EffectSlot.STATIC,
                TargetingRestrictionEffect.hexproofFromCardTypes(Set.of(CardType.INSTANT)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 1),
                new StaticBoostEffect(1, 1, Set.of(Keyword.MENACE), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 10),
                new StaticBoostEffect(5, 5, GrantScope.SELF)));
    }
}
