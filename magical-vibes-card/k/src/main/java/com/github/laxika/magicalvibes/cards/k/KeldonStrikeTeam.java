package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DMU", collectorNumber = "136")
public class KeldonStrikeTeam extends Card {

    public KeldonStrikeTeam() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{W}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), CreateTokenEffect.whiteSoldier(2)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceEnteredBattlefieldThisTurn(),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)));
    }
}
