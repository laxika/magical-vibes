package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "9")
public class GruesomeSlaughter extends Card {

    public GruesomeSlaughter() {
        addSpellEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(DealDamageEffect.toTargetCreature(new SourcePower())),
                        "{T}: This creature deals damage equal to its power to target creature."
                ),
                Subjects.otherCreaturesYouControl(),
                new PermanentIsColorlessPredicate(),
                Duration.UNTIL_END_OF_TURN
        ));
    }
}
