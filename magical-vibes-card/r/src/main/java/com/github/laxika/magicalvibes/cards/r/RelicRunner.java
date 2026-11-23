package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "DOM", collectorNumber = "62")
@CardRegistration(set = "2XM", collectorNumber = "63")
public class RelicRunner extends Card {

    public RelicRunner() {
        addStaticEffect(new ConditionalEffect(new ControllerCastSpellThisTurn(new CardIsHistoricPredicate()),
                new GrantEffectEffect(new CantBeBlockedEffect(), Subjects.source())));
    }
}
