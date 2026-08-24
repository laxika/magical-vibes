package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "107")
public class LastStand extends Card {

    public LastStand() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new LoseLifeEffect(new Scaled(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER), 2),
                LoseLifeRecipient.TARGET_PLAYER));

        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER)));

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER),
                "Saproling", 1, 1, CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS), CountScope.CONTROLLER), 2)));

        PermanentCount islands = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new DrawCardEffect(islands));
        addEffect(EffectSlot.SPELL, new DiscardEffect(islands, DiscardRecipient.CONTROLLER));
    }
}
