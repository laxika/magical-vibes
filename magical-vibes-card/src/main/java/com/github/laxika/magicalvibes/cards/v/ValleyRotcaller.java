package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "119")
public class ValleyRotcaller extends Card {

    public ValleyRotcaller() {
        PermanentCount otherValleyCreatures = new PermanentCount(
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.SQUIRREL,
                        CardSubtype.BAT,
                        CardSubtype.LIZARD,
                        CardSubtype.RAT)),
                CountScope.CONTROLLER,
                true);
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new LoseLifeEffect(otherValleyCreatures, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(otherValleyCreatures)));
    }
}
