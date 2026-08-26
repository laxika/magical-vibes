package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "208")
public class InsidiousRoots extends Card {

    public InsidiousRoots() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.OWN_CREATURES, new PermanentIsTokenPredicate()));

        addEffect(EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD, SequenceEffect.of(
                new CreateTokenEffect("Plant", 0, 1, CardColor.GREEN, List.of(CardSubtype.PLANT), Set.of(), Set.of()),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentHasSubtypePredicate(CardSubtype.PLANT))));
    }
}
