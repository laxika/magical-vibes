package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "144")
public class TyranidHarridan extends Card {

    public TyranidHarridan() {
        // Whenever this creature or another Tyranid you control deals combat damage to a player,
        // create a 1/1 blue Tyranid Gargoyle creature token with flying.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.TYRANID),
                        new CreateTokenEffect(
                                "Tyranid Gargoyle", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.TYRANID, CardSubtype.GARGOYLE),
                                Set.of(Keyword.FLYING), Set.of())));
    }
}
