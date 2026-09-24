package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EOS", collectorNumber = "31")
@CardRegistration(set = "EOS", collectorNumber = "76")
@CardRegistration(set = "EOS", collectorNumber = "121")
@CardRegistration(set = "EOS", collectorNumber = "166")
@CardRegistration(set = "ECC", collectorNumber = "155")
public class NestingGrounds extends Card {

    public NestingGrounds() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}, {T}: Move a counter from target permanent you control onto a second target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MoveCounterFromTargetCreatureToTargetCreatureEffect()),
                "{1}, {T}: Move a counter from target permanent you control onto a second target permanent. "
                        + "Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED,
                List.of(TargetFilters.permanentYouControl(), TargetFilters.permanent()),
                2,
                2
        ));
    }
}
