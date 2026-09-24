package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "25")
public class MischievousLookout extends Card {

    public MischievousLookout() {
        CardAllOfPredicate noncreatureNonAuraPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.AURA))));

        addEffect(EffectSlot.STATIC, new PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
                noncreatureNonAuraPermanent,
                new GrantTriggeredAbilityToCastSpellEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PerpetuallyBecomeCreatureEffect(2, 1, CardSubtype.RAT))));
    }
}
