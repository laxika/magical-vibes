package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "274")
@CardRegistration(set = "TDC", collectorNumber = "209")
@CardRegistration(set = "MKC", collectorNumber = "150")
@CardRegistration(set = "C17", collectorNumber = "24")
public class CurseOfOpulence extends Card {

    public CurseOfOpulence() {
        CreateTokenEffect gold = goldToken();
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        SequenceEffect.of(
                                gold,
                                new ConditionalEffect(
                                        new AllOf(java.util.List.of(new AttackingPlayerIsOpponent(), new AttacksEnchantedPlayer())),
                                        new CreateTokenForTriggeringPlayerEffect(gold)))));
    }

    private static CreateTokenEffect goldToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Gold", List.of(CardSubtype.GOLD), List.of(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "Sacrifice this token: Add one mana of any color."
        )));
    }
}
