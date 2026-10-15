package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "14")
@CardRegistration(set = "MIC", collectorNumber = "52")
public class EmptyTheLaboratory extends Card {

    public EmptyTheLaboratory() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new XValue(), new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                SacrificeRecipient.CONTROLLER).withRecordedSacrificeCount());
        addEffect(EffectSlot.SPELL, new RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
                new EventValue(),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.ZOMBIE))),
                false, true));
    }
}
