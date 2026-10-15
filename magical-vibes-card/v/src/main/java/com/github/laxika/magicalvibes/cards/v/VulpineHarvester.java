package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "19")
@CardRegistration(set = "MOC", collectorNumber = "106")
public class VulpineHarvester extends Card {

    public VulpineHarvester() {
        var phyrexianAttacker = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN),
                new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN)),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.ARTIFACT))
                        .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                        .targetGraveyard(true)
                        .dynamicMaxManaValue(new TotalPowerOfControlledCreatures(phyrexianAttacker))
                        .build()));
    }
}
