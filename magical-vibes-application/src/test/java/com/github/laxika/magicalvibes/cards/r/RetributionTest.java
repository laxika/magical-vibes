package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetributionTest extends BaseCardTest {

    private void castRetribution(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The targeted creatures' controller chooses which one to sacrifice")
    void opponentChoosesWhichCreatureToSacrifice() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        castRetribution(bears, giant);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
    }

    @Test
    @DisplayName("The chosen creature is sacrificed and the other gets a -1/-1 counter")
    void sacrificesChosenAndPutsCounterOnOther() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        castRetribution(bears, giant);
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(giant.getEffectivePower()).isEqualTo(2);
        assertThat(giant.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 counter can be lethal to the surviving creature")
    void counterCanKillTheSurvivor() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent shrunk = addCreatureReady(player2, new GrizzlyBears());
        shrunk.setToughnessModifier(-1);

        castRetribution(bears, shrunk);
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With only one target left legal, that one is sacrificed and no counter is placed")
    void singleRemainingTargetIsSacrificedWithoutCounter() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(bears.getId(), giant.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(giant);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creatures you control are not legal targets")
    void cannotTargetYourOwnCreatures() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
