package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTheVaultsTest extends BaseCardTest {

    private void castOpenTheVaults() {
        harness.setHand(player1, new ArrayList<>(List.of(new OpenTheVaults())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns artifacts from controller's graveyard to battlefield")
    void returnsArtifactsFromControllerGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card artifact = new DarksteelRelic();
        gd.playerGraveyards.get(player1.getId()).add(artifact);

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Returns enchantments from controller's graveyard to battlefield")
    void returnsEnchantmentsFromControllerGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card enchantment = new GloriousAnthem();
        gd.playerGraveyards.get(player1.getId()).add(enchantment);

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Returns artifacts and enchantments from opponent's graveyard under opponent's control")
    void returnsFromOpponentGraveyardUnderOpponentControl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentArtifact = new DarksteelRelic();
        Card opponentEnchantment = new GloriousAnthem();
        gd.playerGraveyards.get(player2.getId()).addAll(List.of(opponentArtifact, opponentEnchantment));

        castOpenTheVaults();

        // Should be on opponent's battlefield, not controller's
        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns cards from both graveyards simultaneously")
    void returnsFromBothGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card myArtifact = new DarksteelRelic();
        Card theirEnchantment = new GloriousAnthem();
        gd.playerGraveyards.get(player1.getId()).add(myArtifact);
        gd.playerGraveyards.get(player2.getId()).add(theirEnchantment);

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Does not return creatures from graveyards")
    void doesNotReturnCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(creature);

        castOpenTheVaults();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Does not return lands from graveyards")
    void doesNotReturnLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card land = new Mountain();
        gd.playerGraveyards.get(player1.getId()).add(land);

        castOpenTheVaults();

        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Works with empty graveyards")
    void worksWithEmptyGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castOpenTheVaults();

        // Only the spell itself should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Open the Vaults"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns only artifacts and enchantments, leaves creatures and lands in graveyard")
    void selectiveReturn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card artifact = new DarksteelRelic();
        Card creature = new GrizzlyBears();
        Card land = new Mountain();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(artifact, creature, land));

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature, land)
                .anyMatch(c -> c.getName().equals("Open the Vaults"))
                .hasSize(3);
    }
}
