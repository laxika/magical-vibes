package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "MH2", collectorNumber = "178")
public class ThrastaTempestsRoar extends Card {

    public ThrastaTempestsRoar() {
        // This spell costs {3} less to cast for each other spell cast this turn.
        addStaticEffect(new ReduceOwnCastCostEffect(
                new Scaled(new SpellsCastThisTurn(CountScope.ANY_PLAYER), 3)));

        // Trample over planeswalkers (CR 702.19c); Scryfall lists no keyword for it.
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE_OVER_PLANESWALKERS, GrantScope.SELF));

        // Thrasta has hexproof as long as it entered the battlefield this turn.
        addStaticEffect(new ConditionalEffect(
                new SourceEnteredBattlefieldThisTurn(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
    }
}
