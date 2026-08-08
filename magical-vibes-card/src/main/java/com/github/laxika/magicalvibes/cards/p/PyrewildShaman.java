package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "36")
public class PyrewildShaman extends Card {

    public PyrewildShaman() {
        // Bloodrush — {1}{R}, Discard this card: Target attacking creature gets +3/+1 until end of
        // turn. The discard is the intrinsic cost of a hand activated ability.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}",
                List.of(new BoostTargetCreatureEffect(3, 1)),
                "Bloodrush — {1}{R}, Discard this card: Target attacking creature gets +3/+1 until end of turn.",
                TargetFilters.attackingCreature()));

        // Whenever one or more creatures you control deal combat damage to a player, if this card is
        // in your graveyard, you may pay {3}. If you do, return this card to your hand.
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayPayManaEffect("{3}", new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Pay {3} to return Pyrewild Shaman from your graveyard to your hand?"),
                        false,
                        true));
    }
}
