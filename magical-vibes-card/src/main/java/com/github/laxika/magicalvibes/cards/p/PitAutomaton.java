package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextExhaustAbilityThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "238")
public class PitAutomaton extends Card {

    public PitAutomaton() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 2, new ManaRestriction.Abilities())),
                "{T}: Add {C}{C}. Spend this mana only to activate abilities."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CopyNextExhaustAbilityThisTurnEffect()),
                "{2}, {T}: When you next activate an exhaust ability that isn't a mana ability this turn, copy it."
                        + " You may choose new targets for the copy."
        ));
    }
}
