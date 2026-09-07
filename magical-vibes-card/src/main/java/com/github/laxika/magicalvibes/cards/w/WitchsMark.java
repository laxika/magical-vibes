package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "158")
public class WitchsMark extends Card {

    public WitchsMark() {
        addEffect(EffectSlot.SPELL,
                new MayEffect(
                        new DiscardAndDrawCardEffect(1, 2),
                        "Discard a card to draw two cards?"));
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.SPELL,
                        new CreateTokenAttachedToTargetEffect(wickedRoleToken()));
    }

    private static CreateTokenEffect wickedRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Wicked",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(
                        EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                        EffectSlot.ON_DEATH, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
