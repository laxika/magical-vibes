package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "84")
public class YouFindTheVillainsLair extends Card {

    public YouFindTheVillainsLair() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Foil Their Scheme — Counter target spell",
                        new CounterSpellEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Learn Their Secrets — Draw two cards, then discard two cards",
                        List.of(new DrawCardEffect(2), new DiscardEffect(2, DiscardRecipient.CONTROLLER)))
        )));
    }
}
