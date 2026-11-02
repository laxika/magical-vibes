package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "135")
public class GoblinBattleJester extends Card {

    public GoblinBattleJester() {
        // Whenever you cast a red spell, target creature can't block this turn.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                null,
                TargetFilters.creature()
        ));
    }
}
