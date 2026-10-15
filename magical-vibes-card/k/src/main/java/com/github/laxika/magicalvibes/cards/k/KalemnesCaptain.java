package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "C15", collectorNumber = "5")
@CardRegistration(set = "C20", collectorNumber = "92")
@CardRegistration(set = "CM2", collectorNumber = "31")
public class KalemnesCaptain extends Card {

    public KalemnesCaptain() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}{W}",
                List.of(new MonstrosityEffect(3)),
                "{5}{W}{W}: Monstrosity 3."
        ));

        addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, new ExileAllPermanentsEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()))));
    }
}
