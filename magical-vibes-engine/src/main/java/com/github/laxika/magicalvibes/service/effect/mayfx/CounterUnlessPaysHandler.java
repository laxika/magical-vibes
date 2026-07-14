package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Counter-unless-pays ({@code PAY_MANA} ransom) — e.g. Mana Leak, Chancellor of the Annex. The old
 * chain matched the {@code CounterUnlessEffect} interface then switched on {@code ransomKind()}; this
 * bean is keyed on the concrete {@link CounterUnlessPaysEffect} whose ransom kind is always
 * {@code PAY_MANA}.
 */
@Component
@RequiredArgsConstructor
public class CounterUnlessPaysHandler implements MayEffectHandlerBean {

    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessPaysEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayPenaltyChoiceHandlerService.handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
    }
}
