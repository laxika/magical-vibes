package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "124")
public class CharmingScoundrel extends Card {

    public CharmingScoundrel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Discard a card, then draw a card",
                        new DiscardAndDrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Treasure token",
                        CreateTokenEffect.ofTreasureToken(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Wicked Role token attached to target creature you control",
                        new CreateTokenAttachedToTargetEffect(wickedRoleToken()),
                        TargetFilters.creatureYouControl())
        )));
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
                        EffectSlot.STATIC, SequenceEffect.of(
                                new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                                new GrantKeywordEffect(Keyword.MENACE, GrantScope.ENCHANTED_CREATURE)),
                        EffectSlot.ON_DEATH, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
