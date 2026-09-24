package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "93")
@CardRegistration(set = "MSC", collectorNumber = "414")
public class ScarletWitchChaoticAvenger extends Card {

    public ScarletWitchChaoticAvenger() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                ExileTopCardsAndMayCastSpellsEffect.controllerTrackedFaceDown(
                        2,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.HERO),
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                        1));
    }
}
