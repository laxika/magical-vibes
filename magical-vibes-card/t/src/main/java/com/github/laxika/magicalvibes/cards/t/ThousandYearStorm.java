package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.SpellsCastBeforeTriggeringSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "248")
@CardRegistration(set = "GRN", collectorNumber = "207")
@CardRegistration(set = "2X2", collectorNumber = "286")
public class ThousandYearStorm extends Card {

    public ThousandYearStorm() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                new CopyTriggeringSpellEffect()
                        .withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery)));
    }
}
