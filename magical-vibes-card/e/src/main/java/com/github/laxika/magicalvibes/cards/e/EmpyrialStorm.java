package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C18", collectorNumber = "2")
public class EmpyrialStorm extends Card {

    public EmpyrialStorm() {
        // When you cast this spell, copy it for each time you've cast your commander from the
        // command zone this game.
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new CommanderCastsFromCommandZoneThisGame()));

        // Create a 4/4 white Angel creature token with flying.
        addSpellEffect(new CreateTokenEffect(
                "Angel", 4, 4, CardColor.WHITE, List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
