package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "142")
public class MonstrousRage extends Card {

    public MonstrousRage() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.SPELL,
                        new CreateTokenAttachedToTargetEffect(monsterRoleToken(), PlayerRelation.ANY));
    }

    private static CreateTokenEffect monsterRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Monster",
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
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
