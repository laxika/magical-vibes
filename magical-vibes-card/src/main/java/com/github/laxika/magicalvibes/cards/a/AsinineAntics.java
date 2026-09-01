package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttachedToOpposingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "42")
public class AsinineAntics extends Card {

    public AsinineAntics() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}{U}")), null, true));
        addEffect(EffectSlot.SPELL,
                new CreateTokensAttachedToOpposingCreaturesEffect(cursedRoleToken()));
    }

    private static CreateTokenEffect cursedRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Cursed",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
