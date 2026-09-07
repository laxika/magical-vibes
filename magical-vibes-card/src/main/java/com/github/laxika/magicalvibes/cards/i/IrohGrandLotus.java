package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "227")
public class IrohGrandLotus extends Card {

    public IrohGrandLotus() {
        addEffect(EffectSlot.ON_ATTACK, new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 2));

        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        var nonLessonInstantOrSorcery = new CardAllOfPredicate(List.of(
                instantOrSorcery,
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.LESSON))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantFlashbackToGraveyardCardsEffect(nonLessonInstantOrSorcery)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantFlashbackToGraveyardCardsEffect(
                        new CardSubtypePredicate(CardSubtype.LESSON), "{1}")));
    }
}
