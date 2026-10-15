package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "252")
public class UltronArtificialMalevolence extends Card {

    public UltronArtificialMalevolence() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new MayPayManaEffect(
                                "{2}",
                                new CreateTokenCopyOfTargetPermanentEffect(),
                                "Pay {2} to create a token that's a copy of that artifact?"
                        )));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new MayPayManaEffect(
                                "{2}",
                                new CreateTokenCopyOfTargetPermanentEffect(
                                        List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN),
                                        Set.of(CardType.CREATURE), 2, 2, Map.of()),
                                "Pay {2} to create a 2/2 Robot Villain copy of that artifact?"
                        )));
    }
}
