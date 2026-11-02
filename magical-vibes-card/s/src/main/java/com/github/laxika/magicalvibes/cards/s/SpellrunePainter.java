package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "160")
@CardRegistration(set = "DBL", collectorNumber = "160")
public class SpellrunePainter extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)
    ));

    public SpellrunePainter() {
        setBackFaceCard(new SpellruneHowler());

        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                INSTANT_OR_SORCERY,
                List.of(new BoostSelfEffect(1, 1))
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpellruneHowler";
    }
}
