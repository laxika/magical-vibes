package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cobblebrute;
import com.github.laxika.magicalvibes.cards.m.MaritimeGuard;
import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.e.EnshroudingMist;
import com.github.laxika.magicalvibes.cards.j.JacesSanctum;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReaveSoul.class, MaritimeGuard.class, BoggartBrute.class, Cobblebrute.class,
        EnshroudingMist.class, JacesSanctum.class})
class ReaveSoulTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ReaveSoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys a creature with power 3 or less")
    void destroysSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());

        prepare();
        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Maritime Guard");
        harness.assertInGraveyard(player2, "Maritime Guard");
        harness.assertInGraveyard(player1, "Reave Soul");
    }

    @Test
    @DisplayName("Can target a creature with exactly power 3")
    void destroysPowerThreeCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new BoggartBrute());

        prepare();
        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boggart Brute");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetLargeCreature() {
        harness.addToBattlefield(player1, new MaritimeGuard());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new Cobblebrute());

        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, wurm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Allows regeneration")
    void allowsRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());
        bears.setRegenerationShield(1);

        prepare();
        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Maritime Guard");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());

        prepare();
        harness.castSorcery(player1, 0, bears.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Reave Soul");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by its caster")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MaritimeGuard());

        prepare();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Maritime Guard");
        harness.assertInGraveyard(player1, "Maritime Guard");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new MaritimeGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JacesSanctum());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with power 3 or less");
    }

    @Test
    @DisplayName("Uses increased effective power when choosing a target")
    void cannotTargetBoostedCreature() {
        harness.addToBattlefield(player1, new MaritimeGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoggartBrute());
        target.setPowerModifier(1);
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Can destroy a creature whose effective power has been reduced to 3")
    void destroysCreatureWithReducedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cobblebrute());
        target.setPowerModifier(-2);

        prepare();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cobblebrute");
        harness.assertInGraveyard(player2, "Cobblebrute");
    }

    @Test
    @DisplayName("Does not resolve when a response increases the target's power above 3")
    void fizzlesIfTargetPowerIncreases() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoggartBrute());
        harness.setHand(player2, List.of(new EnshroudingMist()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        prepare();
        harness.castSorcery(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boggart Brute");
        harness.assertNotInGraveyard(player2, "Boggart Brute");
        harness.assertInGraveyard(player2, "Enshrouding Mist");
        harness.assertInGraveyard(player1, "Reave Soul");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Reave Soul") && log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot destroy an indestructible creature")
    void doesNotDestroyIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        prepare();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Maritime Guard");
        harness.assertNotInGraveyard(player2, "Maritime Guard");
        harness.assertInGraveyard(player1, "Reave Soul");
    }
}
