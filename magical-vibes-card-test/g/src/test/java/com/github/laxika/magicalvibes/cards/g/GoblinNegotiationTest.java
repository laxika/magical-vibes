package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinNegotiation.class, GrizzlyBears.class})
class GoblinNegotiationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates tokens equal to excess damage")
    void createsTokensForExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveSorcery(player1, 0, 5, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates no tokens when there is no excess damage")
    void createsNoTokensWithoutExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Previously marked damage reduces the damage needed for lethal")
    void countsPreviouslyMarkedDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Zero X deals no damage and creates no tokens")
    void zeroXCreatesNoTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sublethal damage remains marked and creates no tokens")
    void sublethalDamageCreatesNoTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can damage its controller's creature and create tokens for that controller")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinNegotiation()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, 4, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({Shock.class})
    @DisplayName("Creates no tokens if the target leaves before resolution")
    void createsNoTokensWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinNegotiation(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 5, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Negotiation");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
