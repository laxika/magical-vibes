package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.r.RearingEmbermare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OraclesRestoration.class, RearingEmbermare.class})
class OraclesRestorationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts the target, draws a card, and gains 1 life")
    void resolvesBoostDrawAndLife() {
        harness.addToBattlefield(player1, new RearingEmbermare());
        harness.getGameData().playerDecks.get(player1.getId()).add(new RearingEmbermare());
        harness.setHand(player1, List.of(new OraclesRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        UUID bearId = harness.getPermanentId(player1, "Rearing Embermare");
        harness.castAndResolveSorcery(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Boost from Oracle's Restoration wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new RearingEmbermare());
        harness.setHand(player1, List.of(new OraclesRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Rearing Embermare");
        harness.castAndResolveSorcery(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new RearingEmbermare()); // your own creature makes the spell playable
        harness.addToBattlefield(player2, new RearingEmbermare());
        harness.setHand(player1, List.of(new OraclesRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID opponentBearId = harness.getPermanentId(player2, "Rearing Embermare");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentBearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An absent target prevents both the draw and the life gain")
    void removedTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RearingEmbermare());
        harness.setLibrary(player1, List.of(new RearingEmbermare()));
        harness.setHand(player1, List.of(new OraclesRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Oracle's Restoration");
    }

    @Test
    @DisplayName("Losing control of the target prevents every effect")
    void targetChangingControllerPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RearingEmbermare());
        harness.setLibrary(player1, List.of(new RearingEmbermare()));
        harness.setHand(player1, List.of(new OraclesRestoration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Oracle's Restoration");
    }
}
