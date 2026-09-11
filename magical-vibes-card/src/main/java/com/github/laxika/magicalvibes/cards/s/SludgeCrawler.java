package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "98")
public class SludgeCrawler extends Card {

    public SludgeCrawler() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsOfDefendingPlayerLibraryEffect(1));
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BoostSelfEffect(1, 1)),
                "{2}: This creature gets +1/+1 until end of turn."));
    }
}
