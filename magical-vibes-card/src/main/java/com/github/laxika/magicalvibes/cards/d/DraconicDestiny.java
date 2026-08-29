package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "130")
public class DraconicDestiny extends Card {

    public DraconicDestiny() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.FLYING, Keyword.HASTE), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.DRAGON, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{1}",
                                List.of(new BoostSelfEffect(1, 0)),
                                "{1}: This creature gets +1/+0 until end of turn."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
