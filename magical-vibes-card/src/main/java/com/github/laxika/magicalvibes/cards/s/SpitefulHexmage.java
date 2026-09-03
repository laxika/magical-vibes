package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "108")
public class SpitefulHexmage extends Card {

    public SpitefulHexmage() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenAttachedToTargetEffect(cursedRoleToken()));
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
