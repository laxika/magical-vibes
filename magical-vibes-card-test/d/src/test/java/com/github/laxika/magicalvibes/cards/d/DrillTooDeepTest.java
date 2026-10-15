package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.u.UthrosScanship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrillTooDeep.class, EvendoWakingHaven.class, UthrosScanship.class})
class DrillTooDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Puts five charge counters on a Spacecraft or Planet you control")
    void putsChargeCountersOnSpacecraftOrPlanet() {
        Permanent spacecraft = addPermanent(player1, "Test Spacecraft", CardType.ARTIFACT, CardSubtype.SPACECRAFT);
        Permanent planet = addPermanent(player1, "Test Planet", CardType.LAND, CardSubtype.PLANET);
        planet.setCounterCount(CounterType.CHARGE, 2);

        cast(0, planet);

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Puts five charge counters on a Spacecraft")
    void putsChargeCountersOnSpacecraft() {
        Permanent spacecraft = addPermanent(player1, "Test Spacecraft", CardType.ARTIFACT, CardSubtype.SPACECRAFT);

        cast(0, spacecraft);

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        Permanent artifact = addPermanent(player2, "Test Artifact", CardType.ARTIFACT);

        cast(1, artifact);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(artifact.getCard().getId()));
    }

    @Test
    @DisplayName("The counter mode cannot target an opponent's Spacecraft")
    void counterModeRequiresControl() {
        Permanent spacecraft = addPermanent(player2, "Opponent Spacecraft", CardType.ARTIFACT, CardSubtype.SPACECRAFT);
        prepareCast();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(spacecraft.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spacecraft or Planet you control");
    }

    @Test
    @DisplayName("The counter mode cannot target an ordinary artifact")
    void counterModeRequiresSpacecraftOrPlanet() {
        Permanent artifact = addPermanent(player1, "Ordinary Artifact", CardType.ARTIFACT);
        prepareCast();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spacecraft or Planet");
    }

    @Test
    @DisplayName("The destroy mode cannot target a nonartifact Planet")
    void destroyModeRequiresArtifact() {
        Permanent planet = addPermanent(player2, "Test Planet", CardType.LAND, CardSubtype.PLANET);
        prepareCast();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(planet.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Charge counters can turn a stationed Spacecraft into a creature")
    void countersReachSpacecraftStationThreshold() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());
        spacecraft.setCounterCount(CounterType.CHARGE, 3);

        cast(0, spacecraft);

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
        assertThat(gqs.isCreature(gd, spacecraft)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spacecraft);
    }

    @Test
    @DisplayName("Counter mode remains legal for an already animated Spacecraft")
    void countersCanTargetAnimatedSpacecraft() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());
        spacecraft.setCounterCount(CounterType.CHARGE, 8);

        cast(0, spacecraft);

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spacecraft);
    }

    @Test
    @DisplayName("Destroy mode can destroy your own Spacecraft")
    void destroysOwnSpacecraft() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());

        cast(1, spacecraft);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spacecraft);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spacecraft.getCard());
    }

    @Test
    @DisplayName("Counter mode does nothing if control of its Planet changes before resolution")
    void counterModeRechecksControlOnResolution() {
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        planet.setCounterCount(CounterType.CHARGE, 2);
        prepareCast();
        harness.castModalInstant(player1, 0, 0, List.of(planet.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(planet);
        gd.playerBattlefields.get(player2.getId()).add(planet);

        harness.passBothPriorities();

        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planet);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(DrillTooDeep.class::isInstance);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter mode adds charge counters to a real Planet without destroying it")
    void countersOnPlanetPreserveOtherPermanents() {
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());
        planet.setCounterCount(CounterType.CHARGE, 7);

        cast(0, planet);

        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(planet, spacecraft);
    }

    @Test
    @DisplayName("Counter mode does nothing if its Spacecraft is destroyed in response")
    void destroyedTargetDoesNotReceiveCounters() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());
        spacecraft.setCounterCount(CounterType.CHARGE, 2);
        prepareCast();
        harness.castModalInstant(player1, 0, 0, List.of(spacecraft.getId()));
        harness.setHand(player2, List.of(new DrillTooDeep()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player2, 0, 1, List.of(spacecraft.getId()));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spacecraft);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spacecraft.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(DrillTooDeep.class::isInstance);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(DrillTooDeep.class::isInstance);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int mode, Permanent target) {
        prepareCast();
        harness.castModalInstant(player1, 0, mode, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DrillTooDeep()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
