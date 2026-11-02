package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "97")
public class PlanarChaos extends Card {

    public PlanarChaos() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new FlipCoinWinEffect(null, new SacrificeSelfEffect()));
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(null,
                        List.of(FlipCoinWinEffect.forTriggeringSpellController(null, new CounterSpellEffect()))));
    }
}
