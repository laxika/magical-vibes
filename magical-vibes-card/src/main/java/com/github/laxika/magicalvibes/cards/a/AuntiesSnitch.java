package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "57")
public class AuntiesSnitch extends Card {

    public AuntiesSnitch() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // Prowl {1}{B}: cast for this cost if you dealt combat damage to a player this turn with a
        // Goblin or Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}")),
                Set.of(CardSubtype.GOBLIN, CardSubtype.ROGUE)));

        // Whenever a Goblin or Rogue you control deals combat damage to a player, if this card is in
        // your graveyard, you may return this card to your hand.
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                                new PermanentHasSubtypePredicate(CardSubtype.ROGUE))),
                        new MayEffect(
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "return this card from your graveyard to your hand")));
    }
}
