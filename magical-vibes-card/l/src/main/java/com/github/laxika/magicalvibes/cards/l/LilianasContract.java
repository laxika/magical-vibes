package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithDifferentNames;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "107")
@CardRegistration(set = "SLD", collectorNumber = "161")
@CardRegistration(set = "SLD", collectorNumber = "1844")
public class LilianasContract extends Card {

    public LilianasContract() {
        // When this enchantment enters, you draw four cards and you lose 4 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(4));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(4));

        // At the beginning of your upkeep, if you control four or more Demons with different names,
        // you win the game.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConditionalEffect(
                        new ControlsPermanentsWithDifferentNames(4,
                                new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                        new WinGameEffect()));
    }
}
