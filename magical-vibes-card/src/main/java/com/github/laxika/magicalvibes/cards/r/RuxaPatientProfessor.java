package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasNoAbilitiesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNoAbilitiesPredicate;

import java.util.List;

@CardRegistration(set = "SLZ", collectorNumber = "83")
@CardRegistration(set = "SLZ", collectorNumber = "204")
@CardRegistration(set = "SLZ", collectorNumber = "325")
public class RuxaPatientProfessor extends Card {

    public RuxaPatientProfessor() {
        CardPredicate creatureWithNoAbilities = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardHasNoAbilitiesPredicate()));
        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(creatureWithNoAbilities)
                .targetGraveyard(true)
                .build();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnCreature);
        addEffect(EffectSlot.ON_ATTACK, returnCreature);
        PermanentHasNoAbilitiesPredicate noAbilities = new PermanentHasNoAbilitiesPredicate();
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, noAbilities));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new AssignCombatDamageAsThoughUnblockedEffect(),
                GrantScope.ALL_OWN_CREATURES,
                noAbilities));
    }
}
