package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.SourceWasCastWithEscape;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "31")
@CardRegistration(set = "NCC", collectorNumber = "132")
public class SkywayRobber extends Card {

    public SkywayRobber() {
        addCastingOption(new GraveyardCast(null, "{3}{U}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)), null, false, false, true));

        CardPredicate artifactInstantOrSorcery = new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceWasCastWithEscape(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new MayCastCardExiledWithSourceEffect(artifactInstantOrSorcery),
                        GrantScope.SELF)));
    }
}
