package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "46")
public class MistformSliver extends Card {

    public MistformSliver() {
        ActivatedAbility becomeCreatureType = new ActivatedAbility(
                false,
                "{1}",
                List.of(new SourceBecomesChosenSubtypeUntilEndOfTurnEffect(true)),
                "{1}: This permanent becomes the creature type of your choice in addition to its other types until end of turn."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                becomeCreatureType,
                GrantScope.ALL_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                becomeCreatureType,
                GrantScope.SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)
        ));
    }
}
