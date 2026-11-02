package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "10")
@CardRegistration(set = "MIC", collectorNumber = "48")
public class WallOfMourning extends Card {

    public WallOfMourning() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsToSourceEffect(
                        new Sum(new PlayersInGame(), new Fixed(-1)),
                        true, false, LibraryScope.CONTROLLER, false));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new Coven(), PutCardExiledWithSourceIntoHandEffect.returningToOwner()));
    }
}
