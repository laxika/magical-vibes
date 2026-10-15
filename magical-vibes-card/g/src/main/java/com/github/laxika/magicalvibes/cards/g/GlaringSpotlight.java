package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentCreatureHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "229")
public class GlaringSpotlight extends Card {

    public GlaringSpotlight() {
        addEffect(EffectSlot.STATIC, new IgnoreOpponentCreatureHexproofEffect());

        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new SacrificeSelfCost(),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_CREATURES),
                        MakeAllCreaturesUnblockableEffect.ownCreatures()),
                "{3}, Sacrifice this artifact: Creatures you control gain hexproof until end of turn and can't be blocked this turn."));
    }
}
