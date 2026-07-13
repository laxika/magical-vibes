package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodPetTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Blood Pet adds one black mana immediately")
    void activateAbilityAddsBlackManaImmediately() {
        harness.addToBattlefield(player1, new BloodPet());

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        // The creature is sacrificed to the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blood Pet"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blood Pet"));

        // Mana ability resolves immediately — no stack entry
        assertThat(gd.stack).isEmpty();

        // One black mana in the pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blood Pet can be sacrificed for mana even with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BloodPet());

        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blood Pet"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
