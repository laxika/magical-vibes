package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "138")
@CardRegistration(set = "PIP", collectorNumber = "434")
@CardRegistration(set = "PIP", collectorNumber = "666")
@CardRegistration(set = "PIP", collectorNumber = "962")
public class NukaNukeLauncher extends Card {

    public NukaNukeLauncher() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK,
                new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(
                        EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                        new TargetPlayerSpellCastTriggerEffect(
                                List.of(new GiveTargetPlayerRadCountersEffect(2))),
                        null));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
