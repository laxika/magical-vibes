package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "209")
public class CurseOfOpulence extends Card {

    public CurseOfOpulence() {
        CreateTokenEffect gold = CreateTokenEffect.ofArtifactToken(1, "Gold", List.of(), List.of(
                new ActivatedAbility(
                        false,
                        null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "Sacrifice this token: Add one mana of any color."
                )));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new AttacksEnchantedPlayer(), gold));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new AttacksEnchantedPlayer(),
                        new CreateTokenForTriggeringPlayerEffect(gold)));
    }
}
