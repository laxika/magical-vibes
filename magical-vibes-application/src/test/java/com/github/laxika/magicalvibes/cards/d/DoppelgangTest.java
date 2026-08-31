package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Doppelgang.class, Forest.class, GrizzlyBears.class})
class DoppelgangTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X token copies of each of X target permanents")
    void createsCopiesOfEachTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, 2, List.of(bearsId, forestId));
        harness.passBothPriorities();

        assertThat(countTokenCopies("Grizzly Bears")).isEqualTo(2);
        assertThat(countTokenCopies("Forest")).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("X=0 creates no tokens and requires no targets")
    void xZeroCreatesNoTokens() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(0);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(countTokenCopies("Grizzly Bears")).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires exactly X permanent targets")
    void requiresExactlyXTargets() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Still copies the legal target when another target is gone")
    void copiesRemainingLegalTarget() {
        UUID firstBearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID secondBearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(2);

        harness.castSorcery(player1, 0, 2, List.of(firstBearsId, secondBearsId));
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(firstBearsId));
        harness.passBothPriorities();

        assertThat(countTokenCopies("Grizzly Bears")).isEqualTo(2);
    }

    private void addManaForX(int x) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3 * x);
    }

    private long countTokenCopies(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }
}
