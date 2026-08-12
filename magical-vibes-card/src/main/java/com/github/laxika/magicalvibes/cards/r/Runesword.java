package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "106")
public class Runesword extends Card {

    public Runesword() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new RegisterDelayedSacrificeSourceWhenTargetLeavesEffect(),
                        new PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect(),
                        new ExileCreaturesDamagedBySourceInsteadOfDyingEffect()
                ),
                "{3}, {T}: Target attacking creature gets +2/+0 until end of turn. When that creature leaves the battlefield this turn, sacrifice this artifact. If the creature deals damage to a creature this turn, the creature dealt damage can't be regenerated this turn. If a creature dealt damage by the targeted creature would die this turn, exile that creature instead.",
                TargetFilters.attackingCreature()));
    }
}
