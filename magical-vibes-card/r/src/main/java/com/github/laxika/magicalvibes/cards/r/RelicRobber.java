package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "153")
public class RelicRobber extends Card {

    public RelicRobber() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                        1,
                        "Goblin Construct",
                        0,
                        1,
                        null,
                        List.of(CardSubtype.GOBLIN, CardSubtype.CONSTRUCT),
                        Set.of(),
                        Set.of(CardType.ARTIFACT),
                        Map.of(
                                EffectSlot.STATIC, new CantBlockEffect(),
                                EffectSlot.UPKEEP_TRIGGERED,
                                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                        )
                )));
    }
}
