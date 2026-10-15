package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorrowedTime.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class BorrowedTimeTest extends BaseCardTest {

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BorrowedTime()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls")
    void etbExilesOpponentPermanent() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        castAndResolve(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled card returns when Borrowed Time is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID borrowedTimeId = harness.getPermanentId(player1, "Borrowed Time");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, borrowedTimeId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Removing Borrowed Time before its trigger resolves does not exile the target")
    void sourceLeavesBeforeTriggerResolves() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Borrowed Time");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Borrowed Time");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can exile an opposing enchantment rather than only a creature")
    void exilesOpponentEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BorrowedTime()).getId();
        castAndResolve(targetId);

        harness.assertOnBattlefield(player1, "Borrowed Time");
        harness.assertNotOnBattlefield(player2, "Borrowed Time");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Borrowed Time"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnPermanent() {
        UUID ownBearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownBearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
