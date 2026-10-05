package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SelesnyaLocket;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoinShields.class, GrizzlyBears.class, Shock.class, SelesnyaLocket.class})
class JoinShieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and protects your creatures only")
    void untapsAndProtectsOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        mine.tap();
        theirs.tap();

        cast(player1);

        assertThat(mine.isTapped()).isFalse();
        assertThat(theirs.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target a creature with granted hexproof")
    void opponentCannotTargetProtectedCreature() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, mine.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Hexproof and indestructible wear off at end of turn")
    void protectionsWearOffAtEndOfTurn() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Already untapped creatures also gain protection")
    void protectsAlreadyUntappedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();

        cast(player1);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        for (Permanent creature : List.of(first, second)) {
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Noncreature permanents are neither untapped nor protected")
    void doesNotAffectNoncreaturePermanents() {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new SelesnyaLocket());
        locket.tap();

        cast(player1);

        assertThat(locket.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, locket, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, locket, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Join Shields");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain protection")
    void doesNotProtectCreaturesEnteringLater() {
        cast(player1);

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, later, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protection makes an opponent's pending targeted spell fail to resolve")
    void invalidatesOpponentsPendingTarget() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, mine.getId());

        cast(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(mine.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creatures present at resolution are untapped and protected")
    void includesCreaturesEnteringBeforeResolution() {
        harness.setHand(player1, List.of(new JoinShields()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        Permanent arriving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        arriving.tap();

        harness.passBothPriorities();

        assertThat(arriving.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, arriving, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, arriving, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Controller can target a protected creature and lethal damage does not destroy it")
    void survivesControllersLethalDamageSpell() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mine.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(mine.getMarkedDamage()).isEqualTo(2);
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new JoinShields()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castInstant(player, 0);
        harness.passBothPriorities();
    }
}
