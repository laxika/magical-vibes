package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalReward.class, DuneBeetle.class, LuxaRiverShrine.class, DoomedDissenter.class})
class FinalRewardTest extends BaseCardTest {

    private void castFinalReward(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FinalReward()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Exiles target creature")
    void exilesCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DuneBeetle()).getId();
        castFinalReward(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LuxaRiverShrine()).getId();

        assertThatThrownBy(() -> castFinalReward(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile a creature controlled by the caster")
    void exilesOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new DuneBeetle()).getId();
        castFinalReward(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
        harness.assertNotInGraveyard(player1, "Dune Beetle");
        harness.assertInGraveyard(player1, "Final Reward");
    }

    @Test
    @DisplayName("Exile does not trigger a creature's dies ability")
    void exileDoesNotTriggerDiesAbility() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DoomedDissenter()).getId();
        castFinalReward(targetId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Doomed Dissenter"));
        harness.assertNotInGraveyard(player2, "Doomed Dissenter");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when another spell exiles the target first")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DuneBeetle()).getId();
        castFinalReward(targetId);
        harness.setHand(player1, List.of(new FinalReward()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dune Beetle"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Final Reward"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
