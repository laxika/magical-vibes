package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "119")
@CardRegistration(set = "SLD", collectorNumber = "671")
@CardRegistration(set = "SLD", collectorNumber = "672")
public class QuestingPhelddagrif extends Card {

    public QuestingPhelddagrif() {
        var targetOpponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent");

        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new BoostSelfEffect(1, 1),
                        new TargetOpponentCreatesTokenEffect(new CreateTokenEffect("Hippo", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.HIPPO), Set.of(), Set.of()))),
                "{G}: Questing Phelddagrif gets +1/+1 until end of turn. Target opponent creates a 1/1 green Hippo creature token.",
                targetOpponent));

        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.BLACK, GrantScope.SELF),
                        new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.RED, GrantScope.SELF),
                        new GainLifeEffect(new Fixed(2), GainLifeRecipient.OPPONENT)),
                "{W}: Questing Phelddagrif gains protection from black and from red until end of turn. Target opponent gains 2 life.",
                targetOpponent));

        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                        new MayEffect(new DrawCardForTargetPlayerEffect(1), "Draw a card?", null, MayChoicePlayer.TARGET_PLAYER)),
                "{U}: Questing Phelddagrif gains flying until end of turn. Target opponent may draw a card.",
                targetOpponent));
    }
}
