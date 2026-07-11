package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LightningCrafterTest extends BaseCardTest {

    private void castLightningCrafter() {
        harness.setHand(player1, List.of(new LightningCrafter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no Goblin or Shaman")
    void autoSacrificesWithNoGoblinOrShaman() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castLightningCrafter();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Lightning Crafter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Crafter"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Championing a Goblin exiles it and keeps Lightning Crafter")
    void championingGoblinExilesIt() {
        harness.addToBattlefield(player1, new MoggFanatic());
        castLightningCrafter();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID goblinId = harness.getPermanentId(player1, "Mogg Fanatic");
        harness.handlePermanentChosen(player1, goblinId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lightning Crafter"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mogg Fanatic"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mogg Fanatic"));
    }

    @Test
    @DisplayName("A Shaman also satisfies the champion cost")
    void shamanSatisfiesChampion() {
        harness.addToBattlefield(player1, new AnabaShaman());
        castLightningCrafter();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID shamanId = harness.getPermanentId(player1, "Anaba Shaman");
        harness.handlePermanentChosen(player1, shamanId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lightning Crafter"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Anaba Shaman"));
    }

    @Test
    @DisplayName("Tap ability deals 3 damage to any target")
    void tapDealsThreeDamage() {
        harness.setLife(player2, 20);
        Permanent crafter = addReadyCrafter(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(crafter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addReadyCrafter(Player player) {
        Permanent perm = new Permanent(new LightningCrafter());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
