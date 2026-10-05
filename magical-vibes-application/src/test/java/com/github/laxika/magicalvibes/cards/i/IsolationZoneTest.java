package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IsolationZone.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Naturalize.class})
class IsolationZoneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a target creature an opponent controls")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolveIsolationZone(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB exiles a target enchantment an opponent controls")
    void etbExilesTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID anthemId = harness.getPermanentId(player2, "Glorious Anthem");

        castAndResolveIsolationZone(anthemId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Isolation Zone leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIsolationZone(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID isolationZoneId = harness.getPermanentId(player1, "Isolation Zone");

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, isolationZoneId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Target is not exiled if Isolation Zone leaves before its ETB resolves")
    void sourceLeavesBeforeTriggerResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new IsolationZone()));
        addIsolationZoneMana();
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        UUID isolationZoneId = harness.getPermanentId(player1, "Isolation Zone");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, isolationZoneId);
        harness.assertInGraveyard(player1, "Isolation Zone");
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(bearsId);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.stolenCreatures.put(bearsId, player1.getId());

        castAndResolveIsolationZone(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID isolationZoneId = harness.getPermanentId(player1, "Isolation Zone");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, isolationZoneId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB does not exile an enchantment destroyed in response")
    void targetLeavesBeforeTriggerResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID anthemId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.setHand(player1, List.of(new IsolationZone()));
        addIsolationZoneMana();
        harness.castEnchantment(player1, 0, anthemId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, anthemId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Isolation Zone");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new IsolationZone()));
        addIsolationZoneMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new IsolationZone()));
        addIsolationZoneMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void castAndResolveIsolationZone(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IsolationZone()));
        addIsolationZoneMana();

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addIsolationZoneMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
