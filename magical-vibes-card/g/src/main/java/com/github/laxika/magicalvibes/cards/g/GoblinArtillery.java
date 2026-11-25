package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "138")
public class GoblinArtillery extends Card {

    public GoblinArtillery() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SequenceEffect.simultaneous(DealDamageEffect.toAnyTarget(2), new DealDamageToPlayersEffect(3, Subjects.you()))),
                "{T}: Goblin Artillery deals 2 damage to any target and 3 damage to you."
        ));
    }
}
