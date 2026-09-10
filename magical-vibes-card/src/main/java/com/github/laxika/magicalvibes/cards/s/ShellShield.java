package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "79")
public class ShellShield extends Card {

    public ShellShield() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}"));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(0, 3))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new Kicked(), new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET)));
    }
}
