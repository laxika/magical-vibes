package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HateMirage.class, GrizzlyBears.class, HillGiant.class})
class HateMirageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates hasty token copies of up to two opposing creatures")
    void createsHastyTokenCopiesOfTwoOpposingCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(List.of(bears.getId(), giant.getId()));

        assertThat(tokenCopies(player1)).hasSize(2);
        assertThat(tokenCopies(player1).stream().map(permanent -> permanent.getCard().getName()))
                .containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
        assertThat(tokenCopies(player1))
                .allMatch(permanent -> permanent.getCard().getKeywords().contains(Keyword.HASTE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(bears, giant);
    }

    @Test
    @DisplayName("Exiles the created token copies at the next end step")
    void exilesCreatedTokenCopiesAtNextEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(bears.getId()));
        assertThat(tokenCopies(player1)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Can resolve with no targets")
    void canResolveWithNoTargets() {
        prepareSpell();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).isEmpty();
    }

    private void cast(List<UUID> targetIds) {
        prepareSpell();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new HateMirage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Permanent> tokenCopies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
