package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostControllerDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoostControllerDamageThisTurnEffectHandlerTest extends AbstractDamageHandlerTest {

    private BoostControllerDamageThisTurnEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new BoostControllerDamageThisTurnEffectHandler(gameLogService, amountEvaluationService);
    }

    @Test
    @DisplayName("Floats a colour source damage bonus on the controller until end of turn")
    void setsBonusForController() {
        Card card = createCard("The Flame of Keld");
        BoostControllerDamageThisTurnEffect effect = BoostControllerDamageThisTurnEffect.fromSourcesOfColor(CardColor.RED, 2);
        StackEntry entry = TestStackDoor.triggered(card, player1Id,
                "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

        handler.resolve(gd, entry, effect);

        assertThat(gd.floatingEffectsOnPlayer(player1Id, BoostControllerDamageThisTurnEffect.class))
                .singleElement().satisfies(floating -> {
                    assertThat(floating.effect()).isEqualTo(effect);
                    assertThat(floating.sourceCardName()).isEqualTo("The Flame of Keld");
                    assertThat(floating.duration()).isEqualTo(Duration.UNTIL_END_OF_TURN);
                });
    }

    @Test
    @DisplayName("Keeps each grant as its own effect")
    void keepsEachGrantSeparate() {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("The Flame of Keld", null, player1Id,
                BoostControllerDamageThisTurnEffect.fromSourcesOfColor(CardColor.RED, 2), player1Id,
                Duration.UNTIL_END_OF_TURN));

        Card card = createCard("Second Flame");
        BoostControllerDamageThisTurnEffect effect = BoostControllerDamageThisTurnEffect.fromSourcesOfColor(CardColor.RED, 3);
        StackEntry entry = TestStackDoor.triggered(card, player1Id,
                "Second Flame", new ArrayList<>(List.of(effect)), null);

        handler.resolve(gd, entry, effect);

        assertThat(gd.floatingEffectsOnPlayer(player1Id, BoostControllerDamageThisTurnEffect.class))
                .extracting(floating -> ((BoostControllerDamageThisTurnEffect) floating.effect()).fixedBonus())
                .containsExactly(2, 3);
    }

    @Test
    @DisplayName("Does not give the bonus to another player")
    void doesNotAffectOtherPlayer() {
        Card card = createCard("The Flame of Keld");
        BoostControllerDamageThisTurnEffect effect = BoostControllerDamageThisTurnEffect.fromSourcesOfColor(CardColor.RED, 2);
        StackEntry entry = TestStackDoor.triggered(card, player1Id,
                "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

        handler.resolve(gd, entry, effect);

        assertThat(gd.floatingEffectsOnPlayer(player2Id, BoostControllerDamageThisTurnEffect.class)).isEmpty();
    }

    @Test
    @DisplayName("Fixes a noncombat bonus of X as it resolves")
    void fixesXAsItResolves() {
        Card card = createCard("Taii Wakeen, Perfect Shot");
        BoostControllerDamageThisTurnEffect effect = BoostControllerDamageThisTurnEffect.noncombatPlusX();
        StackEntry entry = TestStackDoor.triggered(card, player1Id,
                "Taii Wakeen, Perfect Shot", new ArrayList<>(List.of(effect)), null);
        entry.setXValue(3);

        handler.resolve(gd, entry, effect);

        assertThat(gd.floatingEffectsOnPlayer(player1Id, BoostControllerDamageThisTurnEffect.class))
                .singleElement().satisfies(floating -> assertThat(floating.effect())
                        .isEqualTo(new BoostControllerDamageThisTurnEffect(new Fixed(3), null, true)));
    }
}
