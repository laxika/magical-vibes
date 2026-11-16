package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "218")
public class QuandrixTheProof extends Card {

    public QuandrixTheProof() {
        // Cascade: when this spell is cast, the controller may cast the first qualifying card from
        // the top of their library without paying its mana cost.
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());

        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))))
                        .castFrom(Zone.HAND).grantedToTheSpell(),
                new CascadeEffect());
    }
}
