package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "143")
public class JetsBrainwashing extends Card {

    public JetsBrainwashing() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        target(TargetFilters.creature())
                .addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET))
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN)))
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        new UntapPermanentsEffect(TapUntapScope.TARGET)))
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)));
        addSpellEffect(CreateTokenEffect.ofClueToken(1).withInvestigate(false));
    }
}
