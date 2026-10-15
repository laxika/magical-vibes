package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "132")
@CardRegistration(set = "WHO", collectorNumber = "418")
@CardRegistration(set = "WHO", collectorNumber = "737")
@CardRegistration(set = "WHO", collectorNumber = "1009")
public class GallifreyStands extends Card {

    private static final CardSubtype DOCTOR = CardSubtype.DOCTOR;

    public GallifreyStands() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnCardsFromControllerGraveyardToHandEffect(
                        new CardSubtypePredicate(DOCTOR), new Fixed(Integer.MAX_VALUE), false));

        CardAllOfPredicate doctorCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(DOCTOR)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new MayEffect(
                        new PutCardToBattlefieldEffect(doctorCreature, "Doctor creature"),
                        "Put a Doctor creature card from your hand onto the battlefield?"),
                new ConditionalEffect(
                        new ControlsPermanentCount(13, new PermanentHasSubtypePredicate(DOCTOR)),
                        new WinGameEffect())));
    }
}
