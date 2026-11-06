package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "WOC", collectorNumber = "9")
@CardRegistration(set = "WOC", collectorNumber = "45")
public class ArchmageOfEchoes extends Card {

    public ArchmageOfEchoes() {
        CardAllOfPredicate qualifyingPermanentSpell = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.FAERIE),
                        new CardSubtypePredicate(CardSubtype.WIZARD)
                ))
        ));

        addTrigger(Triggers.youCastSpell().withFilter(qualifyingPermanentSpell),
                new CopyTriggeringSpellEffect().withoutNewTargetChoice().permanentSpellCopiesAreTokens());
    }
}
