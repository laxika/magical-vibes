package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "30")
public class OpalGuardian extends Card {

    public OpalGuardian() {
        addTrigger(Triggers.opponentCastsSpell(), SpellCastTriggerEffect.withIntervening(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new BecomeCreatureEffect(3, 4, CardSubtype.GARGOYLE,
                        Set.of(Keyword.FLYING), Set.of(CardColor.RED))),
                new SourceIsEnchantment()));
    }
}
