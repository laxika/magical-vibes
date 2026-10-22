package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPlayLandOrCastFreeEffect;

@CardRegistration(set = "SNC", collectorNumber = "232")
public class ZiatorasEnvoy extends Card {

    public ZiatorasEnvoy() {
        addCastingOption(AlternateHandCast.blitz("{2}{B}{R}{G}"));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LookAtTopCardMayPlayLandOrCastFreeEffect(new EventValue()));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CastForAlternateCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new CastForAlternateCost(), new DrawCardEffect(1)));
    }
}
