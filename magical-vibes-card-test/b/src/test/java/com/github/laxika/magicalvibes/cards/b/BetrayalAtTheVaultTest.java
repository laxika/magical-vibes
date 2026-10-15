package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetrayalAtTheVault.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class BetrayalAtTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen creature deals its power to both other target creatures")
    void dealsPowerDamageToBothTargets() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID ownTargetId = harness.addToBattlefieldAndReturn(player1, new LlanowarElves()).getId();
        UUID opponentTargetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, List.of(sourceId, ownTargetId, opponentTargetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The source creature cannot be chosen as a damage target")
    void cannotTargetSourceCreature() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);


        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The two damage targets must be different creatures")
    void cannotTargetSameCreatureTwice() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);


        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, targetId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No damage is dealt if the source creature leaves before resolution")
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, List.of(sourceId, firstTargetId, secondTargetId));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("The remaining damage target is damaged when the other target leaves")
    void damagesRemainingTargetWhenOneTargetLeaves(boolean removeFirstTarget) {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, List.of(sourceId, firstTargetId, secondTargetId));
        UUID removedTargetId = removeFirstTarget ? firstTargetId : secondTargetId;
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(removedTargetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, removeFirstTarget ? "Llanowar Elves" : "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The damage source must be a creature you control")
    void cannotChooseOpponentsCreatureAsSource() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, firstTargetId, secondTargetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No damage is dealt when the source changes to the opponent's control")
    void dealsNoDamageWhenSourceChangesController() {
        var source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, List.of(source.getId(), firstTargetId, secondTargetId));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
}
