package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndAurasUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExileLinkedCardsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "68")
public class TawnossCoffin extends Card {

    public TawnossCoffin() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new ExileTargetCreatureAndAurasUntilSourceLeavesEffect()),
                "{3}, {T}: Exile target creature and all Auras attached to it.",
                TargetFilters.creature()));
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new ReturnExileLinkedCardsEffect());
    }
}
