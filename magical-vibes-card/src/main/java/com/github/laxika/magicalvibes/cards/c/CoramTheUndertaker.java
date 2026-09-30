package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayLandAndCastSpellFromCardsPutIntoGraveyardsFromLibrariesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M3C", collectorNumber = "7")
@CardRegistration(set = "M3C", collectorNumber = "11")
@CardRegistration(set = "M3C", collectorNumber = "19")
@CardRegistration(set = "M3C", collectorNumber = "27")
@CardRegistration(set = "M3C", collectorNumber = "84")
@CardRegistration(set = "M3C", collectorNumber = "85")
@CardRegistration(set = "M3C", collectorNumber = "138")
public class CoramTheUndertaker extends Card {

    public CoramTheUndertaker() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new GreatestPowerAmongCardsInGraveyard(
                        new CardTypePredicate(CardType.CREATURE), CountScope.ANY_PLAYER),
                new Fixed(0), GrantScope.SELF));

        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.EACH_OPPONENT));

        addEffect(EffectSlot.STATIC,
                new PlayLandAndCastSpellFromCardsPutIntoGraveyardsFromLibrariesThisTurnEffect());
    }
}
