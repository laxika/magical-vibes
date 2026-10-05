package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PierceTheSky.class, AirElemental.class, GrizzlyBears.class})
class PierceTheSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pierce the Sky targeting a creature with flying puts it on stack")
    void castingPutsOnStack() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, airElemental.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(PierceTheSky.class);
        assertThat(entry.getTargetId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new AirElemental());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Resolving Pierce the Sky deals 7 damage and destroys a 4/4 flyer")
    void resolvingDealsSevenDamageAndKills() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Pierce the Sky");
    }

    @Test
    @DisplayName("Pierce the Sky fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Pierce the Sky");
    }

    @Test
    @DisplayName("Pierce the Sky deals exactly seven damage to its controller's surviving flyer")
    void dealsExactlySevenDamageToOwnFlyer() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        flyer.setToughnessModifier(4);
        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, flyer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flyer);
        assertThat(flyer.getMarkedDamage()).isEqualTo(7);
        harness.assertInGraveyard(player1, "Pierce the Sky");
    }

    @Test
    @DisplayName("Pierce the Sky does not resolve if its target loses flying")
    void fizzlesIfTargetLosesFlying() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, flyer.getId());
        flyer.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flyer);
        assertThat(flyer.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Pierce the Sky");
    }

    @Test
    @DisplayName("Pierce the Sky can target a creature with granted flying")
    void canTargetCreatureWithGrantedFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.getGrantedKeywords().add(Keyword.FLYING);
        harness.setHand(player1, List.of(new PierceTheSky()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pierce the Sky");
    }
}
