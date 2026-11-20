package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "138")
public class NullhideFerox extends Card {

    public NullhideFerox() {
        addStaticEffect(new NoncreatureSpellsCantBeCastEffect(0, false, false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new LosesAllAbilitiesEffect(Subjects.source(), Duration.UNTIL_END_OF_TURN)),
                "{2}: This creature loses all abilities until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
        addStaticEffect(new EnterBattlefieldOnDiscardEffect());
    }
}
