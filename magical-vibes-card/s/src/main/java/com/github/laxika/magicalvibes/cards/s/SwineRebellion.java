package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect.CardPrintingReference;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "15")
public class SwineRebellion extends Card {

    private static final PermanentHasSubtypePredicate BOAR =
            new PermanentHasSubtypePredicate(CardSubtype.BOAR);
    private static final List<CardPrintingReference> SPELLBOOK = List.of(
            new CardPrintingReference("YWOE", "18"),
            new CardPrintingReference("YWOE", "24"),
            new CardPrintingReference("YWOE", "28"));

    public SwineRebellion() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsDistinctPermanentNamesCount(3, BOAR),
                SequenceEffect.of(
                        new ConjureCardToBattlefieldEffect("YWOE", "18"),
                        new ConjureCardToBattlefieldEffect("YWOE", "24"),
                        new ConjureCardToBattlefieldEffect("YWOE", "28"))));

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new ControlsDistinctPermanentNamesCount(3, BOAR)),
                new ConjureCardsFromSpellbookToHandEffect(
                        SPELLBOOK, 2, new PutChosenCardFromHandOntoBattlefieldEffect())));
    }
}
