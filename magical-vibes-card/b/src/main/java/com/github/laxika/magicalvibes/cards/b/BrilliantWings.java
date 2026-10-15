package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "454")
public class BrilliantWings extends Card {

    public BrilliantWings() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.HEXPROOF),
                                GrantScope.ENCHANTED_CREATURE));

        // Whenever a creature you control enters, you may pay {1}. If you do, attach this Aura to it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new MayPayManaEffect("{1}", new AttachSourceAuraToTargetCreatureEffect(),
                        "Pay {1} to attach Brilliant Wings to that creature?"));
    }
}
