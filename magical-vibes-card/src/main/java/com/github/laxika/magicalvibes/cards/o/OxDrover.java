package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "6")
@CardRegistration(set = "WOC", collectorNumber = "42")
public class OxDrover extends Card {

    public OxDrover() {
        // This creature can't be blocked by Oxen.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentHasSubtypePredicate(CardSubtype.OX)));

        // Whenever this creature enters or attacks, target opponent creates a 2/4 white Ox
        // creature token and you draw a card.
        var opponentTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        var oxToken = new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                "Ox", 2, 4, CardColor.WHITE, List.of(CardSubtype.OX), Set.of(), Set.of()));
        target(opponentTarget)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, oxToken)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1))
                .addEffect(EffectSlot.ON_ATTACK, oxToken)
                .addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect(1));
    }
}
