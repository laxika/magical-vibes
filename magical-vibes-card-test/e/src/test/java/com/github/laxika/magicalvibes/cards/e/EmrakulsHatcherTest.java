package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmrakulsHatcher.class})
class EmrakulsHatcherTest extends BaseCardTest {

    @Test
    @DisplayName("When Emrakul's Hatcher enters, it creates three Eldrazi Spawn tokens")
    void enteringCreatesThreeSpawnTokens() {
        castAndResolve();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(3);
    }

    @Test
    @DisplayName("An Eldrazi Spawn can be sacrificed to add colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castAndResolve();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void createdTokensHaveTheRequiredCharacteristics() {
        castAndResolve();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(3).allSatisfy(spawn -> {
            assertThat(spawn.getCard().isToken()).isTrue();
            assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(spawn.getCard().getColors()).isEmpty();
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
            assertThat(spawn.isTapped()).isFalse();
        });
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    void tokensAreCreatedByTheEnterTriggerRatherThanCastingTheSpell() {
        prepareHandAndMana();
        harness.castCreature(player1, 0);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Emrakul's Hatcher")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(3);
    }

    @Test
    void allNewSpawnCanBeSacrificedImmediatelyWithoutUsingTheStack() {
        castAndResolve();

        for (Permanent spawn : findPermanents(player1, "Eldrazi Spawn")) {
            int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
            harness.activateAbility(player1, spawnIndex, null, null);
            assertThat(gd.stack).isEmpty();
        }

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(findPermanents(player1, "Emrakul's Hatcher")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void castAndResolve() {
        prepareHandAndMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void prepareHandAndMana() {
        harness.setHand(player1, List.of(new EmrakulsHatcher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
