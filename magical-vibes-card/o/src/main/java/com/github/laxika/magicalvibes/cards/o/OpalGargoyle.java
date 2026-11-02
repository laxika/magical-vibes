package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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

@CardRegistration(set = "USG", collectorNumber = "25")
public class OpalGargoyle extends Card {

    public OpalGargoyle() {
        addTrigger(Triggers.opponentCastsSpell(), SpellCastTriggerEffect.withIntervening(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new BecomeCreatureEffect(2, 2, CardSubtype.GARGOYLE, Set.of(Keyword.FLYING))),
                new SourceIsEnchantment()));
    }
}
