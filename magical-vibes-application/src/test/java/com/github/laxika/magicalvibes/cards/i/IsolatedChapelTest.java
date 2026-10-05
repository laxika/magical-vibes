package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsolatedChapel.class, Mountain.class, Plains.class, Swamp.class})
class IsolatedChapelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Mountain)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Plains and a Swamp")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Plains does not satisfy the check")
    void opponentPlainsDoesNotCount() {
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new IsolatedChapel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new IsolatedChapel());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Plains still allows untapped entry")
    void tappedPlainsStillCounts() {
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();

        Permanent chapel = harness.enterBattlefieldAndReturn(player1, new IsolatedChapel());

        assertThat(chapel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Isolated Chapel does not satisfy the land type check")
    void anotherChapelDoesNotCount() {
        harness.addToBattlefield(player1, new IsolatedChapel());

        Permanent chapel = harness.enterBattlefieldAndReturn(player1, new IsolatedChapel());

        assertThat(chapel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Swamp in the graveyard does not satisfy the check")
    void swampInGraveyardDoesNotCount() {
        harness.setGraveyard(player1, List.of(new Swamp()));

        Permanent chapel = harness.enterBattlefieldAndReturn(player1, new IsolatedChapel());

        assertThat(chapel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entry without a land play still checks the entering controller's lands")
    void entryWithoutLandPlayChecksController() {
        harness.addToBattlefield(player1, new Plains());

        Permanent chapel = harness.enterBattlefieldAndReturn(player2, new IsolatedChapel());

        assertThat(chapel.isTapped()).isTrue();
    }

    private Permanent findChapel(Player player) {
        return findPermanent(player, "Isolated Chapel");
    }
}
