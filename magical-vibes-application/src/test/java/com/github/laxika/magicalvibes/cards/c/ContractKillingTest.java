package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.p.PiratesCutlass;
import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ContractKilling.class, JungleDelver.class, PiratesCutlass.class, AdantoVanguard.class})
class ContractKillingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and creates two Treasure tokens")
    void destroysCreatureAndCreatesTreasures() {
        harness.addToBattlefield(player2, new JungleDelver());
        UUID targetId = harness.getPermanentId(player2, "Jungle Delver");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        // Target creature destroyed
        harness.assertNotOnBattlefield(player2, "Jungle Delver");
        harness.assertInGraveyard(player2, "Jungle Delver");

        // Two Treasure tokens created for the caster
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2);
        for (Permanent treasure : treasures) {
            assertThat(treasure.getCard().isToken()).isTrue();
            assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(treasure.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
            assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        }
    }

    @Test
    @DisplayName("Treasure tokens can be sacrificed for mana of any color")
    void treasureTokensProduceMana() {
        harness.addToBattlefield(player2, new JungleDelver());
        UUID targetId = harness.getPermanentId(player2, "Jungle Delver");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        // Two Treasure tokens on battlefield
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2);

        // Activate a Treasure token's ability (tap + sac → mana ability, resolves immediately)
        harness.activateAbility(player1, 0, null, null);

        // Treasure was sacrificed
        List<Permanent> remainingTreasures = findPermanents(player1, "Treasure");
        assertThat(remainingTreasures).hasSize(1);

        // Choose RED mana
        harness.handleListChoice(player1, "RED");

        // Red mana was added
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution — no Treasure tokens created")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new JungleDelver());
        UUID targetId = harness.getPermanentId(player2, "Jungle Delver");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no treasures
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Cannot target non-creature permanents")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new PiratesCutlass());
        UUID cutlassId = harness.getPermanentId(player2, "Pirate's Cutlass");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, cutlassId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new JungleDelver());
        UUID targetId = harness.getPermanentId(player2, "Jungle Delver");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Contract Killing");
    }

    @Test
    @DisplayName("Creates Treasures even when the legal target is indestructible")
    void createsTreasuresWhenTargetCannotBeDestroyed() {
        harness.addToBattlefield(player2, new AdantoVanguard());
        UUID targetId = harness.getPermanentId(player2, "Adanto Vanguard");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Adanto Vanguard");
        harness.assertNotInGraveyard(player2, "Adanto Vanguard");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Treasure");
        harness.assertInGraveyard(player1, "Contract Killing");
    }

    @Test
    @DisplayName("Can destroy a creature you control and still creates two Treasures")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new JungleDelver());
        UUID targetId = harness.getPermanentId(player1, "Jungle Delver");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ContractKilling()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Jungle Delver");
        harness.assertInGraveyard(player1, "Jungle Delver");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Treasure");
    }
}
