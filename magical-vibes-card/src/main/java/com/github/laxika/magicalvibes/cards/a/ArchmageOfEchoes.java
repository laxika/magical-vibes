package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

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

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(
                        qualifyingPermanentSpell,
                        null,
                        null,
                        null,
                        null,
                        Set.of(),
                        null,
                        false,
                        Set.of(),
                        false,
                        false,
                        false,
                        false,
                        null,
                        List.of(),
                        null,
                        true,
                        false
                ));
    }
}
