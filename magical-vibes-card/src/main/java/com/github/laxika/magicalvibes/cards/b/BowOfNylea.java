package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "153")
public class BowOfNylea extends Card {

    private static final String COST = "{1}{G}";

    public BowOfNylea() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES,
                        new PermanentIsAttackingPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "{1}{G}, {T}: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new DealDamageToTargetCreatureEffect(2,
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "{1}{G}, {T}: Bow of Nylea deals 2 damage to target creature with flying."));

        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new GainLifeEffect(3)),
                "{1}{G}, {T}: You gain 3 life."));

        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new PutTargetCardsFromGraveyardOnBottomOfLibraryEffect(null, 4)),
                "{1}{G}, {T}: Put up to four target cards from your graveyard on the bottom of your library in any order."));
    }
}
