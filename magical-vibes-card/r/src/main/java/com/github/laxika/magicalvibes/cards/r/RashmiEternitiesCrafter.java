package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "184")
@CardRegistration(set = "KLR", collectorNumber = "202")
@CardRegistration(set = "DSC", collectorNumber = "231")
@CardRegistration(set = "C20", collectorNumber = "229")
public class RashmiEternitiesCrafter extends Card {

    public RashmiEternitiesCrafter() {
        // "That spell's mana value" is the triggering spell's, as it last existed on the stack once it has left
        // (CR 608.2h); "a spell with lesser mana value" is judged as the card is cast (CR 709.3a).
        addTrigger(Triggers.youCastSpell().nthThisTurn(1),
                LookAtTopCardsAndMayCastEffect.reveal(new Fixed(1))
                        .withSpellManaValueAtMost(new Sum(new TargetSpellManaValue(), new Fixed(-1)))
                        .leavingTheRest(),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay()));
    }
}
