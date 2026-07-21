package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicPurge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.BiolumeSerpent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BiolumeEggTest extends BaseCardTest {

    @Test
    @DisplayName("ETB scries 2")
    void etbScries2() {
        harness.setHand(player1, List.of(new BiolumeEgg()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB scry

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing returns transformed at next end step")
    void sacrificeReturnsTransformedAtNextEndStep() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, bears.getId(), egg.getId());
        harness.passBothPriorities(); // resolve Purge
        harness.passBothPriorities(); // resolve sacrifice trigger, register delayed return

        assertThat(findPermanentOrNull(player1, "Biolume Egg")).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Biolume Egg"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Biolume Serpent");
        assertThat(serpent.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Destroying does not return transformed")
    void destroyDoesNotReturn() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        harness.getPermanentRemovalService().tryDestroyPermanent(gd, egg, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Biolume Egg"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(findPermanentOrNull(player1, "Biolume Serpent")).isNull();
    }

    @Test
    @DisplayName("Serpent becomes unblockable by sacrificing two Islands")
    void serpentUnblockableBySacrificingIslands() {
        Permanent serpent = addTransformedSerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, indexOf(serpent), null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Serpent cannot activate without two Islands")
    void serpentCannotActivateWithoutTwoIslands() {
        Permanent serpent = addTransformedSerpent();
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(serpent), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private Permanent addTransformedSerpent() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        BiolumeSerpent back = new BiolumeSerpent();
        back.setSetCode(perm.getOriginalCard().getSetCode());
        back.setCollectorNumber(perm.getOriginalCard().getCollectorNumber());
        perm.setCard(back);
        perm.setTransformed(true);
        return perm;
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

    private Permanent findPermanentOrNull(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
