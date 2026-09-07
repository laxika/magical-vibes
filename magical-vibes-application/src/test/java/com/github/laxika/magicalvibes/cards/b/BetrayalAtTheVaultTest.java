package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetrayalAtTheVault.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class BetrayalAtTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen creature deals its power to both other target creatures")
    void dealsPowerDamageToBothTargets() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID ownTargetId = harness.getPermanentId(player1, "Llanowar Elves");
        UUID opponentTargetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(sourceId, ownTargetId, opponentTargetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The source creature cannot be chosen as a damage target")
    void cannotTargetSourceCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The two damage targets must be different creatures")
    void cannotTargetSameCreatureTwice() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, targetId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No damage is dealt if the source creature leaves before resolution")
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new BetrayalAtTheVault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID firstTargetId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID secondTargetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(sourceId, firstTargetId, secondTargetId));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
}
