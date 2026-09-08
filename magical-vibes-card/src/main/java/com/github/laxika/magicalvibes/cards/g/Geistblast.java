package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "160")
public class Geistblast extends Card {

    public Geistblast() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new ExileSelfFromGraveyardCost(), new CopySpellEffect()),
                "{2}{U}, Exile this card from your graveyard: Copy target instant or sorcery spell you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByPredicate()
                        )),
                        "Target must be an instant or sorcery spell you control."
                )
        ));
    }
}
