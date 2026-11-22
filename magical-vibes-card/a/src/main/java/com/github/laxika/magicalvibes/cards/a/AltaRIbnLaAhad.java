package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardCounter;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasMemoryCounterPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "45")
@CardRegistration(set = "ACR", collectorNumber = "137")
public class AltaRIbnLaAhad extends Card {

    public AltaRIbnLaAhad() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                ExileGraveyardCardsEffect.upToOneTargetFromControllerGraveyard(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.ASSASSIN)))),
                PutCounterOnExiledCardsEffect.onCardsExiledThisWay(ExiledCardCounter.MEMORY),
                CreateTokenCopyOfCardEffect.of(
                        Subjects.exiledCards(Subjects.you(), new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE), new CardHasMemoryCounterPredicate()))),
                        CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingWithTargetChoiceExiledAtEndOfCombat())));
    }
}
