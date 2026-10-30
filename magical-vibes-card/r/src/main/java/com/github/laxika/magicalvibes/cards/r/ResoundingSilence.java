package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "22")
public class ResoundingSilence extends Card {

    public ResoundingSilence() {
        // Exile target attacking creature.
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());

        // Cycling {5}{G}{W}{U} ({5}{G}{W}{U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, exile up to two target attacking creatures." The targets are
        // announced with the cycling ability and ride on the self-cycle trigger, which resolves
        // before the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{G}{W}{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {5}{G}{W}{U} ({5}{G}{W}{U}, Discard this card: Draw a card.)",
                TargetFilters.attackingCreature(),
                null,
                null,
                null,
                List.of(),
                0,
                2).withCyclingTriggerUpToTargets());
        addEffect(EffectSlot.ON_SELF_CYCLED, new ExileTargetPermanentEffect());
    }
}
