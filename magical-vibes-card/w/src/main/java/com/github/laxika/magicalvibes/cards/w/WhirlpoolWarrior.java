package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "36")
@CardRegistration(set = "PC2", collectorNumber = "29")
@CardRegistration(set = "PCA", collectorNumber = "29")
public class WhirlpoolWarrior extends Card {

    public WhirlpoolWarrior() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ShuffleHandIntoLibraryAndDrawEffect(false));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new ShuffleHandIntoLibraryAndDrawEffect()),
                "{R}, Sacrifice this creature: Each player shuffles the cards from their hand into their library, then draws that many cards."
        ));
    }
}
