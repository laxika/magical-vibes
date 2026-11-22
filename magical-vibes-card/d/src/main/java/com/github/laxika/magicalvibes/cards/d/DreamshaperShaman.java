package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "130")
@CardRegistration(set = "2X2", collectorNumber = "108")
public class DreamshaperShaman extends Card {

    public DreamshaperShaman() {
        var match = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayPayManaAndSacrificePermanentEffect(
                        "{2}{R}",
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        SequenceEffect.of(
                                new RevealCardsFromTopUntilEffect(match),
                                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(match)),
                                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay())),
                        "nonland permanent"));
    }
}
