package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "124")
public class VorpalSword extends Card {

    public VorpalSword() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{B}{B}",
                List.of(new GrantEffectToSourceUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new TargetPlayerLosesGameEffect(null))),
                "Until end of turn, this Equipment gains \"Whenever equipped creature deals combat damage to a player, that player loses the game.\""));

        addActivatedAbility(new EquipActivatedAbility("{B}{B}"));
    }
}
