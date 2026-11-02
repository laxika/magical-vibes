package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class HaukensInsight extends Card {

    public HaukensInsight() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ExileTopCardsToSourceEffect(1));
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, true, 0, null, true, false, true,
                ExileAccessScope.CONTROLLER, false, false, false, false, false, null, false, false,
                false, false, true));
    }
}
