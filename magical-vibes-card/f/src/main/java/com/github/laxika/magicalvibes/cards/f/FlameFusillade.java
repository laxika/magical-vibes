package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "123")
public class FlameFusillade extends Card {

    public FlameFusillade() {
        addSpellEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(DealDamageEffect.toAnyTarget(1)),
                        "{T}: This permanent deals 1 damage to any target."
                ),
                Subjects.permanentsYouControl(),
                null,
                Duration.UNTIL_END_OF_TURN
        ));
    }
}
