package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "DTK", collectorNumber = "219")
public class DragonlordOjutai extends Card {

    public DragonlordOjutai() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
    }
}
