package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DefendingPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "8")
public class BlightwingWhelp extends Card {

    public BlightwingWhelp() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "{B}: Blightwing Whelp gains haste until end of turn."
        ));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SeekCardsToHandEffect(
                new Fixed(1), null,
                new ManaValueBound(new DefendingPlayerPoisonCounters(), true, 0)));
        addEffect(EffectSlot.STATIC, new ToxicEffect(1));
    }
}
