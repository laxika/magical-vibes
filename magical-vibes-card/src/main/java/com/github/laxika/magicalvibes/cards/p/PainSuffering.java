package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "294")
public class PainSuffering extends Card {

    public PainSuffering() {
        CardEffect pain = new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER);
        CardEffect suffering = new DestroyTargetPermanentEffect();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Pain - Target player discards a card",
                        pain
                ).withManaCost("{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Suffering - Destroy target land",
                        suffering,
                        TargetFilters.land()
                ).withManaCost("{3}{R}")
        )));
    }
}
