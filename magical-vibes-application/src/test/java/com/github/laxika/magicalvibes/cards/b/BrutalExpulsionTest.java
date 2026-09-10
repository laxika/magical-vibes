package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalExpulsion.class, GrizzlyBears.class, HillGiant.class, Island.class, Shock.class})
class BrutalExpulsionTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void returnsTargetSpellToItsOwnersHand() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new BrutalExpulsion()));
        addBrutalExpulsionMana(player1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, shockId, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shock");
        harness.assertNotInGraveyard(player2, "Shock");
    }

    @Test
    void damageModeKillsAndExilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void bothModesResolveAgainstTheirTargets() {
        Permanent creatureToReturn = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creatureToDamage = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(new int[]{0, 1}, List.of(creatureToReturn.getId(), creatureToDamage.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(creatureToDamage.getMarkedDamage()).isEqualTo(2);
        assertThat(creatureToDamage.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    void bounceModeRejectsAnIslandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeRejectsAnIslandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new BrutalExpulsion()));
        addBrutalExpulsionMana(player1);
        if (java.util.Arrays.stream(modes).anyMatch(mode -> mode == 0)) {
            harness.castModalInstantWithModes(player1, 0, 1, 2, modes,
                    targetIds.getFirst(), targetIds.subList(1, targetIds.size()));
        } else {
            harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        }
        harness.passBothPriorities();
    }

    private void addBrutalExpulsionMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
