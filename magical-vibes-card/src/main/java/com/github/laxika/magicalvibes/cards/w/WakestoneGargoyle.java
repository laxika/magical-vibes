package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "21")
public class WakestoneGargoyle extends Card {

    public WakestoneGargoyle() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect(
                        new CanAttackAsThoughNoDefenderEffect())),
                "{1}{W}: Creatures you control with defender can attack this turn as though they didn't have defender."));
    }
}
