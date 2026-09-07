package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.l.LionsEyeDiamond;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismaticCircle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmuletOfUnmaking.class, FemerefScouts.class, Plains.class, LionsEyeDiamond.class,
        PrismaticCircle.class})
class AmuletOfUnmakingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability exiles the target creature")
    void exilesTargetCreature() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Femeref Scouts");
        harness.assertNotInGraveyard(player2, "Femeref Scouts");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Femeref Scouts"));
    }

    @Test
    @DisplayName("Can exile a land")
    void exilesTargetLand() {
        addReadyAmulet(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Can exile an artifact")
    void exilesTargetArtifact() {
        addReadyAmulet(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LionsEyeDiamond());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lion's Eye Diamond"));
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact, creature, nor land")
    void cannotTargetIneligiblePermanent() {
        addReadyAmulet(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrismaticCircle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("Amulet is exiled as a cost, not sacrificed")
    void amuletExiledAsCost() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Amulet of Unmaking");
        harness.assertNotInGraveyard(player1, "Amulet of Unmaking");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Amulet of Unmaking"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate during the opponent's turn (sorcery speed only)")
    void cannotActivateAtInstantSpeed() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyAmulet(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AmuletOfUnmaking());
        perm.setSummoningSick(false);
        return perm;
    }
}
