package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "173")
@CardRegistration(set = "DD2", collectorNumber = "36")
@CardRegistration(set = "JVC", collectorNumber = "36")
public class Fireslinger extends Card {

    public Fireslinger() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(SequenceEffect.simultaneous(new DealDamageToAnyTargetEffect(1), new DealDamageToPlayersEffect(1, Subjects.you()))),
                "{T}: Fireslinger deals 1 damage to any target and 1 damage to you."));
    }
}
