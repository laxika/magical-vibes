package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOC", collectorNumber = "23")
@CardRegistration(set = "WOC", collectorNumber = "31")
public class CourtOfLocthwain extends Card {

    public CourtOfLocthwain() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                        new ExileTopCardsToSourceEffect(
                                1, false, false, LibraryScope.TARGET_OPPONENT, true),
                        new ConditionalEffect(
                                new ControllerIsMonarch(),
                                new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), true))));

        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                true, null, false, false, 0, null, false, false, false, true));
    }
}
