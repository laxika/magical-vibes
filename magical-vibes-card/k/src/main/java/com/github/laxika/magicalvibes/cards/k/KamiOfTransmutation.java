package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YNEO", collectorNumber = "3")
public class KamiOfTransmutation extends Card {

    public KamiOfTransmutation() {
        ChooseOneAtTriggerTimeEffect choice = new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each permanent card in your hand perpetually becomes an artifact in addition to its other types.",
                        becomes(CardType.ARTIFACT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each permanent card in your hand perpetually becomes an enchantment in addition to its other types.",
                        becomes(CardType.ENCHANTMENT)))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), choice);
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), choice);
    }

    private static PerpetuallyGrantCardCharacteristicsEffect becomes(CardType type) {
        return new PerpetuallyGrantCardCharacteristicsEffect(Set.of(type), Set.of(), List.of())
                .to(Subjects.cardsInYourHand(new CardIsPermanentPredicate()));
    }
}
