package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "41")
public class SurgeOfSalvation extends Card {

    public SurgeOfSalvation() {
        addSpellEffect(new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.HEXPROOF));
        addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_PERMANENTS));
        addSpellEffect(PreventDamageEffect.fromColorsToControlledCreatures(
                Set.of(CardColor.BLACK, CardColor.RED)));
    }
}
