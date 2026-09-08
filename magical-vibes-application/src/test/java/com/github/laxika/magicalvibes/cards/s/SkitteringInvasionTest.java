package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SkitteringInvasion()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
