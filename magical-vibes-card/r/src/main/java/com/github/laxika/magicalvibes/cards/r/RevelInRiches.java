package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "117")
@CardRegistration(set = "SLD", collectorNumber = "1830")
public class RevelInRiches extends Card {

    public RevelInRiches() {
        // Whenever a creature an opponent controls dies, create a Treasure token.
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), CreateTokenEffect.ofTreasureToken(1));

        // At the beginning of your upkeep, if you control ten or more Treasures, you win the game.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConditionalEffect(new ControlsPermanentCount(10, new PermanentHasSubtypePredicate(CardSubtype.TREASURE)), new WinGameEffect()));
    }
}
