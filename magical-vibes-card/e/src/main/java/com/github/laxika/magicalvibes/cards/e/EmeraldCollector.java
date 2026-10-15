package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "6")
public class EmeraldCollector extends Card {

    public EmeraldCollector() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new OnceOnlyTriggerEffect(new NthCardDrawTriggerEffect(3,
                        new ConjureCardNamedIntoHandEffect("Mox Emerald", false))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF)),
                "{2}{G}: This creature has base power and toughness 4/4 until end of turn."));
    }
}
