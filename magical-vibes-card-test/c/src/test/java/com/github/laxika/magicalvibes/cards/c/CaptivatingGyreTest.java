package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptivatingGyre.class, GrizzlyBears.class, Spellbook.class})
class CaptivatingGyreTest extends BaseCardTest {

    @Test
    @DisplayName("Returns three target creatures to their owners' hands")
    void returnsThreeCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getId())
                .toList();
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, targetIds);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gameData.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Can return fewer than three creatures")
    void returnsOneCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can choose no creatures")
    void returnsNoCreatures() {
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTwoCreaturesWithDifferentControllers() {
        var own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opposing.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(own.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposing.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(unchosen);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Captivating Gyre");
    }

    @Test
    void returnsStolenCreatureToOwner() {
        var stolen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(stolen.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolen.getCard());
    }

    @Test
    void cannotChooseFourCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getId()).toList();
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsRemainingTargetWhenAnotherLeavesBattlefield() {
        var departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castSorcery(player1, 0, List.of(departed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(departed);
        gd.playerGraveyards.get(player2.getId()).add(departed.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(departed.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Captivating Gyre");
    }

    @Test
    void choosingZeroTargetsLeavesAvailableCreaturesAlone() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Captivating Gyre");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReturnUnchosenCreatureWhenAllTargetsLeave() {
        var departed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CaptivatingGyre()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castSorcery(player1, 0, List.of(departed.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(departed);
        gd.playerGraveyards.get(player2.getId()).add(departed.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(unchosen);
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(departed.getCard());
        harness.assertInGraveyard(player1, "Captivating Gyre");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
