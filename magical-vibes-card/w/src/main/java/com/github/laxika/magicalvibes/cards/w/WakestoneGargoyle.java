package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "21")
@CardRegistration(set = "TDC", collectorNumber = "137")
public class WakestoneGargoyle extends Card {

    public WakestoneGargoyle() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new GrantPlayerStaticEffectsUntilEndOfTurnEffect(List.of(
                        new MatchingCreaturesCanAttackAsThoughNoDefenderEffect(
                                new PermanentHasKeywordPredicate(Keyword.DEFENDER))))),
                "{1}{W}: Creatures you control with defender can attack this turn as though they didn't have defender."));
    }
}
