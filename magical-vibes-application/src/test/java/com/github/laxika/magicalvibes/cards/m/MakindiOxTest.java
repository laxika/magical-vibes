package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakindiOx.class, Forest.class, GrizzlyBears.class})
class MakindiOxTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall taps target creature an opponent controls")
    void landfallTapsTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MakindiOx());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new MakindiOx());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Landfall cannot target a creature controlled by Makindi Ox's controller")
    void landfallRejectsOwnCreatureTarget() {
        harness.addToBattlefield(player1, new MakindiOx());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Landfall can target an already tapped opponent creature")
    void landfallCanTargetTappedCreature() {
        harness.addToBattlefield(player1, new MakindiOx());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MakindiOx());
        victim.setTapped(true);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Landfall with no opposing creatures does not leave a target prompt")
    void landfallWithNoLegalTargets() {
        Permanent ox = harness.addToBattlefieldAndReturn(player1, new MakindiOx());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(ox.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Landfall still resolves after Makindi Ox leaves the battlefield")
    void landfallResolvesWithoutSource() {
        Permanent ox = harness.addToBattlefieldAndReturn(player1, new MakindiOx());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MakindiOx());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, victim.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ox);
        gd.playerGraveyards.get(player1.getId()).add(ox.getCard());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
