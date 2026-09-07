package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "228")
public class RovingKeep extends Card {

    public RovingKeep() {
        addActivatedAbility(new ActivatedAbility(false, "{7}",
                List.of(
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF),
                        new CanAttackAsThoughNoDefenderEffect()
                ),
                "{7}: This creature gets +2/+0 and gains trample until end of turn. It can attack this turn as though it didn't have defender."));
    }
}
