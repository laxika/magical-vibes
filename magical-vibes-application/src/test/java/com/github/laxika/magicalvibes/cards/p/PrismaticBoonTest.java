package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrismaticBoonTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 gives both targeted creatures protection from the single chosen color")
    void grantsChosenColorProtectionToAllTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2); // X=2: {2}{W}{U}

        harness.castInstantForX(player1, 0, 2, List.of(bears.getId(), hawk.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can target creatures any player controls")
    void canTargetOpponentCreature() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 2, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(theirs.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1); // X=1

        harness.castInstantForX(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        bears.resetModifiers();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
