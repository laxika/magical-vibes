package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "112")
@CardRegistration(set = "MIC", collectorNumber = "130")
@CardRegistration(set = "DRC", collectorNumber = "100")
@CardRegistration(set = "SCD", collectorNumber = "114")
@CardRegistration(set = "FDC", collectorNumber = "129")
public class UndeadAugur extends Card {

    public UndeadAugur() {
        // Whenever this creature or another Zombie you control dies, you draw a card and you lose
        // 1 life.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
