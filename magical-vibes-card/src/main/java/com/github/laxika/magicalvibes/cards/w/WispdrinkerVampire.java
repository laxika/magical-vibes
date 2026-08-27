package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "243")
@CardRegistration(set = "MKM", collectorNumber = "374")
public class WispdrinkerVampire extends Card {

    private static final SequenceEffect SMALL_CREATURE_TRIGGER = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
            new GainLifeEffect(1));

    public WispdrinkerVampire() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMaxPowerConditionalEffect(2, SMALL_CREATURE_TRIGGER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}{B}",
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK),
                        GrantScope.ALL_OWN_CREATURES,
                        new PermanentPowerAtMostPredicate(2))),
                "{5}{W}{B}: Creatures you control with power 2 or less gain deathtouch and lifelink until end of turn."
        ));
    }
}
