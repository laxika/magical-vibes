package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "140")
public class SabertoothAlleyCat extends Card {

    public SabertoothAlleyCat() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEFENDER),
                        "creatures with defender",
                        true)),
                "{1}{R}: Creatures without defender can't block this creature this turn."
        ));
    }
}
