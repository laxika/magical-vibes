package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "56")
public class Littjara extends Card {
    public Littjara() {
        CreateTokenEffect shapeshifter = new CreateTokenEffect(
                "Shapeshifter", 2, 2, CardColor.BLUE,
                List.of(CardSubtype.SHAPESHIFTER), Set.of(Keyword.CHANGELING), Set.of());
        addTrigger(Triggers.planeswalkToThis(), shapeshifter);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), shapeshifter);
        addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnEachControlledPermanentOfChosenSubtypeEffect());
    }
}
