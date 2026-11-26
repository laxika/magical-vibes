package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "62")
@CardRegistration(set = "MM2", collectorNumber = "112")
public class DragonsoulKnight extends Card {

    public DragonsoulKnight() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{U}{B}{R}{G}", List.of(
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON, Subjects.source()),
                BoostEffect.self(5, 3),
                new GrantKeywordEffect(Keyword.FLYING, Subjects.source()),
                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.source())),
                "{W}{U}{B}{R}{G}: Until end of turn, this creature becomes a Dragon, gets +5/+3, and gains flying and trample."));
    }
}
