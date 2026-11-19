package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LowestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ROE", collectorNumber = "125")
public class RepayInKind extends Card {

    public RepayInKind() {
        addSpellEffect(new SetLifeTotalEffect(
                new LowestLifeTotalAmongPlayers(), Subjects.eachPlayer()));
    }
}
