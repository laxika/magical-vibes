package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;


@CardRegistration(set = "NCC", collectorNumber = "38")
@CardRegistration(set = "NCC", collectorNumber = "139")
@CardRegistration(set = "OTC", collectorNumber = "141")
public class MisfortuneTeller extends Card {

    public MisfortuneTeller() {
        var rogueToken = new CreateTokenEffect(
                "Rogue", 2, 2, CardColor.BLACK, List.of(CardSubtype.ROGUE), Set.of(), Set.of());
        var creature = new CardTypePredicate(CardType.CREATURE);
        var land = new CardTypePredicate(CardType.LAND);
        // "If it was a creature card, ... If it was a land card, ... Otherwise, ...": a creature land card makes both.
        var exileEffect = SequenceEffect.of(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, creature), rogueToken),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, land),
                        CreateTokenEffect.ofTreasureToken(1)),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE,
                        new CardNotPredicate(new CardAnyOfPredicate(List.of(creature, land)))),
                        new GainLifeEffect(3)));

        addTrigger(Triggers.enters(ObjectRelation.THIS), exileEffect);
        addTrigger(Triggers.dealsCombatDamageToPlayer(), exileEffect);
    }
}
