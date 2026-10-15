package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpenTheVaults.class, DarksteelRelic.class, GloriousAnthem.class, GrizzlyBears.class,
        Mountain.class, GlazeFiend.class, Pacifism.class})
class OpenTheVaultsTest extends BaseCardTest {

    private void castOpenTheVaults() {
        harness.castFromHand(player1, new OpenTheVaults(), "{4}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns artifacts from controller's graveyard to battlefield")
    void returnsArtifactsFromControllerGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card artifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(artifact));

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
        harness.setGraveyard(player1, List.of(enchantment));

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
        harness.setGraveyard(player2, List.of(opponentArtifact, opponentEnchantment));

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
        harness.setGraveyard(player1, List.of(myArtifact));
        harness.setGraveyard(player2, List.of(theirEnchantment));

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
        harness.setGraveyard(player1, List.of(creature));

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
        harness.setGraveyard(player1, List.of(land));

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
        harness.setGraveyard(player1, List.of(artifact, creature, land));

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature, land)
                .anyMatch(c -> c.getName().equals("Open the Vaults"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Returned Aura enchants the only legal creature already on the battlefield")
    void returnsAuraAttachedToExistingCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(aura.getId()))
                .singleElement()
                .satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("An Aura with no legal host remains in its graveyard")
    void auraWithoutLegalHostRemainsInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card aura = new Pacifism();
        harness.setGraveyard(player2, List.of(aura));

        castOpenTheVaults();

        harness.assertNotOnBattlefield(player2, "Pacifism");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(aura);
    }

    @Test
    @DisplayName("Returning artifact creatures cannot serve as hosts for returning Auras")
    void auraCannotEnchantCreatureReturningInSameEvent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card creature = new GlazeFiend();
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, aura));

        castOpenTheVaults();

        harness.assertOnBattlefield(player1, "Glaze Fiend");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura).doesNotContain(creature);
    }

    @Test
    @DisplayName("An entering artifact sees another artifact returning in the same event")
    void enteringArtifactSeesSimultaneousArtifactEntry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card artifact = new DarksteelRelic();
        Card fiend = new GlazeFiend();
        harness.setGraveyard(player1, List.of(artifact, fiend));

        castOpenTheVaults();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(fiend.getId()))
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getPowerModifier()).isEqualTo(2);
                    assertThat(p.getToughnessModifier()).isEqualTo(2);
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact, fiend);
    }
}
