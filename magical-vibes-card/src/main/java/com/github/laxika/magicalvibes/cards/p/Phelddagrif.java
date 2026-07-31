package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Set;

/**
 * Each ability pairs a benefit for the controller with a gift for the opponent. The opponent is
 * derived rather than targeted — this engine is two-player, so "target opponent" has exactly one
 * legal choice.
 */
@CardRegistration(set = "ALL", collectorNumber = "115")
public class Phelddagrif extends Card {

    public Phelddagrif() {
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF),
                        new TargetOpponentCreatesTokenEffect(new CreateTokenEffect("Hippo", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.HIPPO), Set.of(), Set.of()))),
                "{G}: Phelddagrif gains trample until end of turn. Target opponent creates a 1/1 green Hippo creature token."));

        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                        new GainLifeEffect(new Fixed(2), GainLifeRecipient.OPPONENT)),
                "{W}: Phelddagrif gains flying until end of turn. Target opponent gains 2 life."));

        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(ReturnToHandEffect.self(), new TargetOpponentMayDrawCardEffect()),
                "{U}: Return Phelddagrif to its owner's hand. Target opponent may draw a card."));
    }
}
