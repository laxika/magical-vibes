package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "454")
public class BrilliantWings extends Card {

    public BrilliantWings() {
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.HEXPROOF),
                                GrantScope.ENCHANTED_CREATURE));

        // Whenever a creature you control enters, you may pay {1}. If you do, attach this Aura to it.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayPayManaEffect("{1}", AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(),
                        "Pay {1} to attach Brilliant Wings to that creature?"));
    }
}
