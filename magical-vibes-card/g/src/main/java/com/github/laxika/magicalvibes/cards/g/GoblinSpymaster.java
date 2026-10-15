package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "C16", collectorNumber = "19")
public class GoblinSpymaster extends Card {

    public GoblinSpymaster() {
        // At the beginning of each opponent's end step, that player creates a 1/1 red Goblin
        // creature token with "Creatures you control attack each combat if able."
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new CreateTokenForTriggeringPlayerEffect(
                new CreateTokenEffect(
                        1,
                        "Goblin",
                        1,
                        1,
                        CardColor.RED,
                        List.of(CardSubtype.GOBLIN),
                        Set.of(),
                        Set.of(),
                        Map.of(EffectSlot.STATIC, new MustAttackEffect(GrantScope.ALL_OWN_CREATURES))
                )));
    }
}
