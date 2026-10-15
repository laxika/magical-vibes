package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "3")
@CardRegistration(set = "WOC", collectorNumber = "34")
public class AlelaCunningConqueror extends Card {

    public AlelaCunningConqueror() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(new CreateTokenEffect("Faerie Rogue", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.FAERIE, CardSubtype.ROGUE),
                                Set.of(Keyword.FLYING), Set.of())),
                        null, null, null, true, false, null, 1));

        PermanentPredicate defendingPlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()));
        target(new PermanentPredicateTargetFilter(
                defendingPlayerCreature,
                "Target must be a creature that player controls"))
                .addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                        new AllyCombatDamageTriggerEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.FAERIE),
                                new GoadTargetCreatureUntilNextTurnEffect(),
                                false,
                                true));
    }
}
