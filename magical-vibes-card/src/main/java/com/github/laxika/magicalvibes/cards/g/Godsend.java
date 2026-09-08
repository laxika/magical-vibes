package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfCombatOpponentsAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "JOU", collectorNumber = "12")
public class Godsend extends Card {

    public Godsend() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new CantCastSpellsWithSameNameAsExiledCardEffect(true));
        addEffect(EffectSlot.ON_BLOCK,
                new MayEffect(new ExileOneOfCombatOpponentsAndTrackWithSourceEffect(),
                        "Exile one of those creatures?"), TriggerMode.ONCE_PER_BLOCK);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new MayEffect(new ExileOneOfCombatOpponentsAndTrackWithSourceEffect(),
                        "Exile one of those creatures?"), TriggerMode.ONCE_PER_BLOCK);
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
