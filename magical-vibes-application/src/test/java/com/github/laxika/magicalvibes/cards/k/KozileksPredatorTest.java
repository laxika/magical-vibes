package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozileksPredator.class})
class KozileksPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("When Kozilek's Predator enters, it creates two Eldrazi Spawn tokens")
    void enteringCreatesTwoSpawnTokens() {
        castKozileksPredator();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    @DisplayName("An Eldrazi Spawn can be sacrificed to add colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castKozileksPredator();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void castKozileksPredator() {
        harness.setHand(player1, List.of(new KozileksPredator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Spawn are created when the enter trigger resolves, not when the creature spell resolves")
    void tokensWaitForEnterTriggerResolution() {
        harness.setHand(player1, List.of(new KozileksPredator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kozilek's Predator");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Created Spawn are untapped 0/1 colorless Eldrazi Spawn creatures")
    void createdTokensHaveOracleCharacteristics() {
        castKozileksPredator();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2).allSatisfy(spawn -> {
            assertThat(spawn.getCard().isToken()).isTrue();
            assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spawn.getCard().getPower()).isZero();
            assertThat(spawn.getCard().getToughness()).isEqualTo(1);
            assertThat(spawn.getCard().getColors()).isEmpty();
            assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(spawn.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Both newly created Spawn can produce mana immediately, even while tapped")
    void bothSpawnCanBeSacrificedWithoutUsingTheStack() {
        castKozileksPredator();

        for (Permanent spawn : List.copyOf(findPermanents(player1, "Eldrazi Spawn"))) {
            spawn.setTapped(true);
            int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
            harness.activateAbility(player1, spawnIndex, null, null);
            assertThat(gd.stack).isEmpty();
        }

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertOnBattlefield(player1, "Kozilek's Predator");
    }
}
