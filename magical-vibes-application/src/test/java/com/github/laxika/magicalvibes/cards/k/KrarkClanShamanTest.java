package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrarkClanShaman.class, KrarkClanGrunt.class, Ornithopter.class, WeldingJar.class})
class KrarkClanShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact deals 1 damage to each creature without flying")
    void sacrificesArtifactAndDamagesNonFlyers() {
        harness.addToBattlefield(player1, new KrarkClanShaman());
        harness.addToBattlefield(player1, new WeldingJar());
        harness.addToBattlefield(player2, new KrarkClanGrunt());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new WeldingJar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Welding Jar");
        harness.assertInGraveyard(player1, "Krark-Clan Shaman");
        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.assertNotOnBattlefield(player1, "Krark-Clan Shaman");
        assertThat(findPermanent(player2, "Krark-Clan Grunt").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Ornithopter").getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Welding Jar");
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new KrarkClanShaman());
        harness.addToBattlefield(player1, new KrarkClanGrunt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new KrarkClanShaman());
        harness.addToBattlefield(player2, new WeldingJar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Welding Jar");
    }

    @Test
    @DisplayName("Stacked activations still deal damage after the Shaman dies")
    void stackedActivationsResolveAfterSourceDies() {
        harness.addToBattlefield(player1, new KrarkClanShaman());
        harness.addToBattlefield(player1, new WeldingJar());
        harness.addToBattlefield(player1, new WeldingJar());
        harness.addToBattlefield(player1, new KrarkClanGrunt());
        harness.addToBattlefield(player2, new KrarkClanGrunt());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Welding Jar"));
        harness.assertInGraveyard(player1, "Welding Jar");
        assertThat(findPermanent(player2, "Krark-Clan Grunt").getMarkedDamage()).isZero();

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Krark-Clan Shaman");
        assertThat(findPermanent(player1, "Krark-Clan Grunt").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Krark-Clan Grunt").getMarkedDamage()).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Krark-Clan Grunt");
        harness.assertInGraveyard(player2, "Krark-Clan Grunt");
        assertThat(findPermanent(player2, "Ornithopter").getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
