package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispatchTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Taps target creature without metalcraft")
    void tapsTargetCreatureWithoutMetalcraft() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not exile target creature without metalcraft")
    void doesNotExileWithoutMetalcraft() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Taps and exiles target creature with metalcraft")
    void tapsAndExilesWithMetalcraft() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isNotEmpty();
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("Only taps if metalcraft lost before resolution")
    void onlyTapsIfMetalcraftLostBeforeResolution() {
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new Dispatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        addThreeArtifacts(player1);

        harness.castInstant(player1, 0, creature.getId());

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook") || p.getCard().getName().equals("Leonin Scimitar"));

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    // ===== Helpers =====

    private void addThreeArtifacts(Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
