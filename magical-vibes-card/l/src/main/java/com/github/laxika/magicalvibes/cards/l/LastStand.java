package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DeterminedOnce;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
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
@CardRegistration(set = "PC2", collectorNumber = "100")
@CardRegistration(set = "PCA", collectorNumber = "100")
public class LastStand extends Card {

    public LastStand() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new LoseLifeEffect(new Scaled(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER), 2),
                LoseLifeRecipient.TARGET_PLAYER));

        target(TargetFilters.creature()).addSpellEffect(new DealDamageToTargetCreatureEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER)));

        addSpellEffect(new CreateTokenEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER),
                "Saproling", 1, 1, CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
        addSpellEffect(new GainLifeEffect(new Scaled(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS), CountScope.CONTROLLER), 2)));

        DynamicAmount islands = new DeterminedOnce(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER));
        addSpellEffect(new DrawCardEffect(islands));
        addSpellEffect(new DiscardEffect(islands, DiscardRecipient.CONTROLLER));
    }
}
