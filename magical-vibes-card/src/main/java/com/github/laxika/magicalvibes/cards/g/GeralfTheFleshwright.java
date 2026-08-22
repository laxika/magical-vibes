package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "50")
public class GeralfTheFleshwright extends Card {

    public GeralfTheFleshwright() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.atLeastDuringYourTurn(2, null, List.of(
                        new CreateTokenEffect("Zombie Rogue", 2, 2, CardColor.BLUE,
                                Set.of(CardColor.BLUE, CardColor.BLACK),
                                List.of(CardSubtype.ZOMBIE, CardSubtype.ROGUE)))));

        var otherZombiesEnteredThisTurn = new Sum(
                new PermanentsEnteredBattlefieldThisTurn(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER),
                new Fixed(-1));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new PutCountersOnEnteringCreatureEffect(otherZombiesEnteredThisTurn, false)));
    }
}
