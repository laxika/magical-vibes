package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Doppelgang.class, DoublingSeason.class, Forest.class, GrizzlyBears.class})
class DoppelgangTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X token copies of each of X target permanents")
    void createsCopiesOfEachTargetPermanent() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(2);

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

    @Test
    @CardUsed({Doppelgang.class, DoublingSeason.class, Forest.class})
    void copiedDoublingSeasonsDoNotMultiplyTokensFromTheSameSpell() {
        UUID seasonId = harness.addToBattlefieldAndReturn(player2, new DoublingSeason()).getId();
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(2);

        harness.castSorcery(player1, 0, 2, List.of(seasonId, forestId));
        harness.passBothPriorities();

        assertThat(countTokenCopies("Doubling Season")).isEqualTo(2);
        assertThat(countTokenCopies("Forest")).isEqualTo(2);
    }

    @Test
    void copiedLandEntersUntappedAndCanProduceMana() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new Forest());
        original.tap();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(1);

        harness.castSorcery(player1, 0, 1, List.of(original.getId()));
        harness.passBothPriorities();

        assertThat(countTokenCopies("Forest")).isEqualTo(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(original.isTapped()).isTrue();
    }

    @Test
    void createsNothingWhenAllTargetsHaveLeft() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(1);

        harness.castSorcery(player1, 0, 1, List.of(targetId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Doppelgang");
    }

    @Test
    void copiesFaceDownCharacteristicsInsteadOfHiddenLand() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Forest());
        original.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(1);

        harness.castSorcery(player1, 0, 1, List.of(original.getId()));
        harness.passBothPriorities();

        assertThat(countTokenCopies("Forest")).isZero();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isFaceDown()).isFalse();
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void permitsMoreThanOneHundredTargetsWhenXRequiresThem() {
        List<UUID> targetIds = java.util.stream.IntStream.range(0, 101)
                .mapToObj(ignored -> harness.addToBattlefieldAndReturn(player2, new Forest()).getId())
                .toList();
        harness.setHand(player1, List.of(new Doppelgang()));
        addManaForX(101);

        harness.castSorcery(player1, 0, 101, targetIds);

        assertThat(gd.stack).hasSize(1);
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
