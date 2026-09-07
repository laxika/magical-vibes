package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BirthingHulk.class})
class BirthingHulkTest extends BaseCardTest {

    @Test
    @DisplayName("When Birthing Hulk enters, it creates two Eldrazi Scion tokens")
    void enteringCreatesTwoScions() {
        castAndResolve();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("An Eldrazi Scion can be sacrificed to add colorless mana")
    void scionSacrificeAddsColorlessMana() {
        castAndResolve();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);

        harness.activateAbility(player1, scionIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {1}{C} grants Birthing Hulk a regeneration shield")
    void regeneratesSelf() {
        castAndResolve();
        Permanent hulk = findPermanent(player1, "Birthing Hulk");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hulk), 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getRegenerationShield()).isEqualTo(1);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new BirthingHulk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
