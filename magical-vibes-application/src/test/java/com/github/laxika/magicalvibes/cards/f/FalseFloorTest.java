package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FalseFloor.class, FountainOfYouth.class, GrizzlyBears.class})
class FalseFloorTest extends BaseCardTest {

    @Test
    @DisplayName("False Floor and all creatures enter tapped")
    void permanentsEnterTapped() {
        harness.setHand(player1, List.of(new FalseFloor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent floor = findPermanent(player1, "False Floor");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        Permanent opposingCreature = findPermanent(player2, "Grizzly Bears");

        assertThat(floor.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability exiles only untapped creatures")
    void abilityExilesOnlyUntappedCreatures() {
        Permanent floor = addReadyFalseFloor(player1);
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent untappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untappedOpposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOwnCreature.tap();
        untappedOwnCreature.untap();
        untappedOpposingCreature.untap();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "False Floor");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(untappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(untappedOpposingCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("False Floor", "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void abilityRequiresSorcerySpeed() {
        addReadyFalseFloor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyFalseFloor(Player player) {
        Permanent floor = harness.addToBattlefieldAndReturn(player, new FalseFloor());
        floor.untap();
        return floor;
    }
}
