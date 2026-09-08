package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismaticBoon.class, BayFalcon.class, MtendaHerder.class, Forest.class})
class PrismaticBoonTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 gives both targeted creatures protection from the single chosen color")
    void grantsChosenColorProtectionToAllTargets() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new MtendaHerder());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2); // X=2: {2}{W}{U}

        harness.castInstantForX(player1, 0, 2, List.of(falcon.getId(), herder.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(falcon.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(herder.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can target creatures any player controls")
    void canTargetOpponentCreature() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new MtendaHerder());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 2, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(theirs.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("X=2 requires exactly two creature targets")
    void requiresExactlyXTargets() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(falcon.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 2");
    }

    @Test
    @CardUsed(Incinerate.class)
    @DisplayName("Protection from the chosen color prevents a matching spell from targeting the creature")
    void protectionFromChosenColorPreventsMatchingSpellTarget() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1); // X=1: {1}{W}{U}

        harness.castInstantForX(player1, 0, 1, List.of(falcon.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1); // {1}{R}

        assertThatThrownBy(() -> harness.castInstant(player2, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("X=0 still requires the color choice before resolving")
    void xZeroStillRequiresColorChoice() {
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        harness.setHand(player1, List.of(new PrismaticBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1); // X=1

        harness.castInstantForX(player1, 0, 1, List.of(falcon.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(falcon.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        falcon.resetModifiers();
        assertThat(falcon.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
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
