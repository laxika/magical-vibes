package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "52")
public class TemporalCascade extends Card {

    public TemporalCascade() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player shuffles their hand and graveyard into their library",
                        new EachPlayerShufflesZonesIntoLibraryEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player draws seven cards",
                        new EachPlayerDrawsCardEffect(7))
        )));
    }
}
