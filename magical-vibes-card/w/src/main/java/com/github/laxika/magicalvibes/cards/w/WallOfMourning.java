package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;

@CardRegistration(set = "MIC", collectorNumber = "10")
@CardRegistration(set = "MIC", collectorNumber = "48")
public class WallOfMourning extends Card {

    public WallOfMourning() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsToSourceEffect(
                        new Sum(new PlayersInGame(), new Fixed(-1)),
                        true, false, LibraryScope.CONTROLLER, false));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new Coven(), new PutCardExiledWithSourceIntoHandEffect()));
    }
}
