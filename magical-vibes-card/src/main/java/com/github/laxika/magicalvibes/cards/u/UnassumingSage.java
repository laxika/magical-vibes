package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "37")
public class UnassumingSage extends Card {

    public UnassumingSage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayPayManaEffect(
                "{2}",
                new CreateTokenAttachedToSourceEffect(sorcererRoleToken()),
                "Pay {2} to create a Sorcerer Role token?"));
    }

    private static CreateTokenEffect sorcererRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Sorcerer",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_ATTACK,
                                new ScryEffect(1),
                                GrantScope.ENCHANTED_CREATURE))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
