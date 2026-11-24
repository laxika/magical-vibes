package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;

@CardRegistration(set = "DIS", collectorNumber = "49")
public class Nightcreep extends Card {

    public Nightcreep() {
        addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLACK, Subjects.otherCreatures()));
        addSpellEffect(GrantBasicLandTypeToTargetEffect.of(
                Subjects.allLands(), CardSubtype.SWAMP, true, Duration.UNTIL_END_OF_TURN));
    }
}
