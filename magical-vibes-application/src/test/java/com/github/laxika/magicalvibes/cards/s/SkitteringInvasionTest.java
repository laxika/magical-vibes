package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SkitteringInvasion.class})
class SkitteringInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates five 0/1 colorless Eldrazi Spawn creature tokens")
    void createsFiveEldraziSpawnTokens() {
        castAndResolve();

        List<Permanent> spawns = findPermanents(player1, "Eldrazi Spawn");
        assertThat(spawns).hasSize(5);
        assertThat(spawns).allSatisfy(spawn -> {
            assertThat(spawn.getCard().isToken()).isTrue();
            assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spawn.getCard().getColor()).isNull();
            assertThat(spawn.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("An Eldrazi Spawn can be sacrificed for one colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castAndResolve();

        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4);
    }

    @Test
    @DisplayName("All five newly created Spawn can immediately produce mana without using the stack")
    void allSpawnsCanImmediatelyProduceMana() {
        castAndResolve();

        for (int i = 0; i < 5; i++) {
            Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
            int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
            harness.activateAbility(player1, spawnIndex, 0, null, null);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                    .isEqualTo(i + 1);
            assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4 - i);
        }
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Spawn can still be sacrificed for mana")
    void tappedSpawnCanProduceMana() {
        castAndResolve();

        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        spawn.tap();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spawn);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new SkitteringInvasion(), "{7}");
        harness.passBothPriorities();
    }
}
