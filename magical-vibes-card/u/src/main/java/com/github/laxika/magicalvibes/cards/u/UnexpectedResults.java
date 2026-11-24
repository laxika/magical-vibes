package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "GTC", collectorNumber = "203")
@CardRegistration(set = "PIO", collectorNumber = "397")
public class UnexpectedResults extends Card {

    public UnexpectedResults() {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addSpellEffect(new ShuffleLibraryEffect(false));
        // A land is never offered for the cast, and a card not cast stays on top.
        addSpellEffect(LookAtTopCardsAndMayCastEffect.reveal(new Fixed(1)).leavingTheRest());
        // The spell returns only if the land was put onto the battlefield (ruling).
        addSpellEffect(new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, land), new MayEffect(
                SequenceEffect.of(
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(land)),
                        new ConditionalEffect(HappenedThisWay.returned(), ReturnToHandEffect.selfSpell())),
                "Put the revealed land onto the battlefield and return Unexpected Results to its owner's hand?")));
    }
}
