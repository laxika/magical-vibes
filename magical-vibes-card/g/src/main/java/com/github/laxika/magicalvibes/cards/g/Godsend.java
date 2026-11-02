package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfCombatOpponentsAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "12")
public class Godsend extends Card {

    public Godsend() {
        addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new CantCastSpellsWithSameNameAsExiledCardEffect(true));
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK,
                new MayEffect(new ExileOneOfCombatOpponentsAndTrackWithSourceEffect(),
                        "Exile one of those creatures?"));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.ONCE_PER_BLOCK,
                new MayEffect(new ExileOneOfCombatOpponentsAndTrackWithSourceEffect(),
                        "Exile one of those creatures?"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
