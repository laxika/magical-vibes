package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "162")
public class IshkanahGrafwidow extends Card {

    public IshkanahGrafwidow() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new Delirium(),
                new CreateTokenEffect(3, "Spider", 1, 2, CardColor.GREEN,
                        List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.of())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{B}",
                List.of(new LoseLifeEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SPIDER),
                                CountScope.CONTROLLER),
                        LoseLifeRecipient.TARGET_PLAYER)),
                "{6}{B}: Target opponent loses 1 life for each Spider you control.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
