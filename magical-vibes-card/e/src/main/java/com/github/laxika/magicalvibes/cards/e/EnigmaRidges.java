package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "48")
public class EnigmaRidges extends Card {

    public EnigmaRidges() {
        addTrigger(Triggers.planeswalkToThis(),
                new EachPlayerMaySearchLibraryForBasicLandsToHandEffect());
        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                new DrawCardEffect(1),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                        "Put a land card from your hand onto the battlefield?")));
    }
}
