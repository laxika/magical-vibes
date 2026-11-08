package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "278")
@CardRegistration(set = "MB2", collectorNumber = "514")
public class Friarball extends Card {

    public Friarball() {
        addSpellEffect(new CreateTokenEffect(
                "Monk", 2, 2, CardColor.WHITE, List.of(CardSubtype.MONK), Set.of(), Set.of()));
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn()));
    }
}
