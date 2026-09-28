package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "367")
@CardRegistration(set = "MB2", collectorNumber = "606")
public class LuxiorIgnited extends Card {

    public LuxiorIgnited() {
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new CountersOnSource(CounterType.ANY),
                new CountersOnSource(CounterType.ANY),
                GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new AttachSourceEquipmentToTargetCreatureEffect()),
                "+1: Attach this Equipment to up to one target creature you control.",
                TargetFilters.creatureYouControl(),
                +1, null, null, List.of(), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(
                        2, 2, Keyword.DOUBLE_STRIKE)),
                "−2: Equipped creature gets +2/+2 and gains double strike until end of turn."));
    }
}
