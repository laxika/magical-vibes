package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Counter-unless-discards ({@code DISCARD_CARD} ransom, e.g. Ward—Discard a card). The old chain
 * matched the {@code CounterUnlessEffect} interface then switched on {@code ransomKind()}; this bean
 * is keyed on the concrete {@link CounterUnlessDiscardsEffect} whose ransom kind is always
 * {@code DISCARD_CARD}.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessDiscardsHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessDiscardsEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayPenaltyChoiceHandlerService.handleCounterUnlessDiscardsChoice(gameData, player, accepted, ability);
    }
}
