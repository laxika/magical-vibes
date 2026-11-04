package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "171")
public class AbstractPaintmage extends Card {

    public AbstractPaintmage() {
        // At the beginning of your first main phase, add {U}{R}.
        // Spend this mana only to cast instant and sorcery spells.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))),
                new AwardRestrictedManaEffect(ManaColor.RED, 1, new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))));
    }
}
