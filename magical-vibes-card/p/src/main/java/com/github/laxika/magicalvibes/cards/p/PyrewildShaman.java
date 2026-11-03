package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "36")
@CardRegistration(set = "MM3", collectorNumber = "106")
@CardRegistration(set = "2XM", collectorNumber = "138")
public class PyrewildShaman extends Card {

    public PyrewildShaman() {
        // Bloodrush — {1}{R}, Discard this card: Target attacking creature gets +3/+1 until end of
        // turn. The discard is the intrinsic cost of a hand activated ability.
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(false, "{1}{R}",
                List.of(new BoostTargetCreatureEffect(3, 1)),
                "Bloodrush — {1}{R}, Discard this card: Target attacking creature gets +3/+1 until end of turn.",
                TargetFilters.attackingCreature()));

        // Whenever one or more creatures you control deal combat damage to a player, if this card is
        // in your graveyard, you may pay {3}. If you do, return this card to your hand.
        addTrigger(new TriggerCondition(Triggers.creatureYouControlDealsCombatDamageToPlayer(), AbilityZones.GRAVEYARD),
                TriggerOptions.ONE_OR_MORE.withInterveningIf(new SourceCardInGraveyard()),
                new MayPayManaEffect("{3}", new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {3} to return Pyrewild Shaman from your graveyard to your hand?"));
    }
}
