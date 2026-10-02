package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherChanneler.class, Forest.class, AcademyWall.class, Island.class})
class AetherChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("The token mode creates a 1/1 white Bird with flying")
    void createsBirdToken() {
        cast(0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird"))
                .singleElement()
                .satisfies(bird -> {
                    assertThat(bird.getCard().getPower()).isEqualTo(1);
                    assertThat(bird.getCard().getToughness()).isEqualTo(1);
                    assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
                });
    }

    @Test
    @DisplayName("The draw mode draws a card")
    void drawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        cast(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("The bounce mode returns another nonland permanent to its owner's hand")
    void returnsAnotherNonlandPermanent() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AcademyWall());
        cast(1, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Academy Wall");
        harness.assertInHand(player2, "Academy Wall");
    }

    @Test
    @DisplayName("The bounce mode rejects a land target")
    void rejectsLandTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(1, island.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Create a 1/1 white Bird creature token with flying");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bird");
    }

    @Test
    @DisplayName("The bounce mode cannot target Aether Channeler itself")
    void cannotBounceItself() {
        harness.setHand(player1, List.of(new AetherChanneler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1,
                "Return another target nonland permanent to its owner's hand"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Create a 1/1 white Bird creature token with flying");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bird");
    }

    @Test
    @DisplayName("Another Aether Channeler is a legal bounce target")
    void canBounceAnotherChanneler() {
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AetherChanneler());
        cast(1, other.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aether Channeler");
        harness.assertInHand(player2, "Aether Channeler");
        harness.assertOnBattlefield(player1, "Aether Channeler");
    }

    @Test
    @DisplayName("The bounce mode can return a permanent controlled by its controller")
    void canBounceOwnPermanent() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new AcademyWall());
        cast(1, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Academy Wall");
        harness.assertInHand(player1, "Academy Wall");
    }

    private void cast(int mode, UUID... targetIds) {
        harness.setHand(player1, List.of(new AetherChanneler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, switch (mode) {
            case 0 -> "Create a 1/1 white Bird creature token with flying";
            case 1 -> "Return another target nonland permanent to its owner's hand";
            case 2 -> "Draw a card";
            default -> throw new IllegalArgumentException("Unknown mode");
        });
        if (targetIds.length > 0) {
            harness.handlePermanentChosen(player1, targetIds[0]);
        }
    }

    @Test
    @DisplayName("Entering without being cast still lets the controller choose the draw mode")
    void choosesDrawWhenNotCast() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new AetherChanneler());
        harness.passPriority(player1);
        harness.handleListChoice(player1, "Draw a card");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
