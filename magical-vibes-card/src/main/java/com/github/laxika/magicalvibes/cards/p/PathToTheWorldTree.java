package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "186")
public class PathToTheWorldTree extends Card {

    public PathToTheWorldTree() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(CardPredicateUtils.basicLand()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{U}{B}{R}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new GainLifeEffect(2),
                        new DrawCardEffect(2),
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                        new DealDamageToTargetCreatureEffect(2),
                        new CreateTokenEffect("Bear", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.BEAR), Set.of(), Set.of())
                ),
                "{2}{W}{U}{B}{R}{G}, Sacrifice this enchantment: You gain 2 life and draw two cards. Target opponent loses 2 life. This enchantment deals 2 damage to up to one target creature. You create a 2/2 green Bear creature token.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        TargetFilters.creature()
                ),
                1,
                2
        ));
    }
}
