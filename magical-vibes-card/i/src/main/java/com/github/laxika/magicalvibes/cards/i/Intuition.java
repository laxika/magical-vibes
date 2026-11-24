package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.IntuitionEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TMP", collectorNumber = "70")
@CardRegistration(set = "TPR", collectorNumber = "54")
public class Intuition extends Card {

    public Intuition() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        ))
                .addSpellEffect(IntuitionEffect.cards(3))
                .addSpellEffect(ChooseCardEffect.oneOf(Subjects.revealedThisWay(), "a card to put into the hand")
                        .by(Subjects.targetPlayerDeclaredElsewhere()))
                .addSpellEffect(new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))
                .addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()))
                .addSpellEffect(new ShuffleLibraryEffect(false));
    }
}
