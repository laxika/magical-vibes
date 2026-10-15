package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.w.WolfSkullShaman;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningCrafter.class, ElvishWarrior.class, PricklyBoggart.class, WolfSkullShaman.class})
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
        harness.addToBattlefield(player1, new ElvishWarrior());
        castLightningCrafter();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Lightning Crafter");
        harness.assertInGraveyard(player1, "Lightning Crafter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Championing a Goblin exiles it and keeps Lightning Crafter")
    void championingGoblinExilesIt() {
        harness.addToBattlefield(player1, new PricklyBoggart());
        castLightningCrafter();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID goblinId = harness.getPermanentId(player1, "Prickly Boggart");
        harness.handlePermanentChosen(player1, goblinId);

        harness.assertOnBattlefield(player1, "Lightning Crafter");
        harness.assertNotOnBattlefield(player1, "Prickly Boggart");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Prickly Boggart"));
    }

    @Test
    @DisplayName("A Shaman also satisfies the champion cost")
    void shamanSatisfiesChampion() {
        harness.addToBattlefield(player1, new WolfSkullShaman());
        castLightningCrafter();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID shamanId = harness.getPermanentId(player1, "Wolf-Skull Shaman");
        harness.handlePermanentChosen(player1, shamanId);

        harness.assertOnBattlefield(player1, "Lightning Crafter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wolf-Skull Shaman"));
    }

    @Test
    @DisplayName("The championed creature returns when Lightning Crafter leaves")
    void championedCreatureReturnsWhenCrafterLeaves() {
        harness.addToBattlefield(player1, new PricklyBoggart());
        castLightningCrafter();
        harness.passBothPriorities();

        UUID goblinId = harness.getPermanentId(player1, "Prickly Boggart");
        harness.handlePermanentChosen(player1, goblinId);
        Permanent crafter = findPermanent(player1, "Lightning Crafter");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, crafter));

        harness.assertNotOnBattlefield(player1, "Lightning Crafter");
        harness.assertNotOnBattlefield(player1, "Prickly Boggart");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Prickly Boggart");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Prickly Boggart"));
    }

    @Test
    @DisplayName("Controller may sacrifice Lightning Crafter instead of championing an eligible Goblin")
    void mayDeclineChampionWithEligibleGoblin() {
        harness.addToBattlefield(player1, new PricklyBoggart());
        castLightningCrafter();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Lightning Crafter");
        harness.assertInGraveyard(player1, "Lightning Crafter");
        harness.assertOnBattlefield(player1, "Prickly Boggart");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Goblin cannot satisfy champion")
    void opponentsGoblinCannotBeChampioned() {
        harness.addToBattlefield(player2, new PricklyBoggart());
        castLightningCrafter();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Crafter");
        harness.assertOnBattlefield(player2, "Prickly Boggart");
    }

    @Test
    @DisplayName("Activated damage still resolves after Lightning Crafter leaves")
    void damageResolvesAfterSourceLeaves() {
        Permanent crafter = addCreatureReady(player1, new LightningCrafter());
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, crafter));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Crafter");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Tap ability deals 3 damage to any target")
    void tapDealsThreeDamage() {
        harness.setLife(player2, 20);
        Permanent crafter = addCreatureReady(player1, new LightningCrafter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(crafter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Tap ability deals 3 damage to a creature")
    void tapDealsThreeDamageToCreature() {
        Permanent crafter = addCreatureReady(player1, new LightningCrafter());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(crafter.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }
}
