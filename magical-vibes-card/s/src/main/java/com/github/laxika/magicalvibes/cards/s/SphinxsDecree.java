package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellTypesNextTurnEffect;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "24")
public class SphinxsDecree extends Card {

    public SphinxsDecree() {
        addSpellEffect(new OpponentsCantCastSpellTypesNextTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
