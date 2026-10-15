package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Colored mana cannot pay the colorless part of regeneration")
    void regenerationRequiresColorlessMana() {
        castAndResolve();
        Permanent hulk = findPermanent(player1, "Birthing Hulk");
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hulk), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hulk.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both newly created Scions can immediately pay for regeneration")
    void scionsPayForRegeneration() {
        castAndResolve();
        Permanent hulk = findPermanent(player1, "Birthing Hulk");

        for (Permanent scion : findPermanents(player1, "Eldrazi Scion")) {
            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(scion), null, null);
            assertThat(gd.stack).isEmpty();
        }
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hulk), 0, null, null);
        assertThat(hulk.getRegenerationShield()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hulk.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Regeneration prevents lethal damage destruction and consumes the shield")
    void regenerationSavesHulkFromLethalDamage() {
        castAndResolve();
        Permanent hulk = findPermanent(player1, "Birthing Hulk");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hulk), 0, null, null);
        harness.passBothPriorities();
        assertThat(hulk.isTapped()).isFalse();

        hulk.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(findPermanent(player1, "Birthing Hulk")).isSameAs(hulk);
        assertThat(hulk.isTapped()).isTrue();
        assertThat(hulk.getMarkedDamage()).isZero();
        assertThat(hulk.getRegenerationShield()).isZero();

        hulk.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Birthing Hulk")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hulk.getCard());
    }
}
