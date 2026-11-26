package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "45")
public class PemminsAura extends Card {

    public PemminsAura() {
        target(TargetFilters.creature());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new UntapPermanentsEffect(Subjects.attached())),
                "{U}: Untap enchanted creature."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature(),
                        Duration.UNTIL_END_OF_TURN)),
                "{U}: Enchanted creature gains flying until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, Subjects.attachedCreature(),
                        Duration.UNTIL_END_OF_TURN)),
                "{U}: Enchanted creature gains shroud until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Enchanted creature gets +1/-1 until end of turn",
                                BoostEffect.of(Subjects.attached(), 1, -1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Enchanted creature gets -1/+1 until end of turn",
                                BoostEffect.of(Subjects.attached(), -1, 1))
                ))),
                "{1}: Enchanted creature gets +1/-1 or -1/+1 until end of turn."
        ));
    }
}
