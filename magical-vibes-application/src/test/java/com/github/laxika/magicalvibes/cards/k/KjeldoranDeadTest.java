package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KjeldoranDeadTest extends BaseCardTest {

    // ===== ETB: sacrifice a creature =====

    @Test
    @DisplayName("ETB sacrifices Kjeldoran Dead itself when it is the only creature")
    void etbSacrificesItselfWhenOnlyCreature() {
        harness.setHand(player1, List.of(new KjeldoranDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kjeldoran Dead"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kjeldoran Dead"));
    }

    @Test
    @DisplayName("ETB lets controller choose which creature to sacrifice; Kjeldoran Dead can be spared")
    void etbControllerChoosesToSacrificeOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new KjeldoranDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Kjeldoran Dead");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== {B}: Regenerate =====

    @Test
    @DisplayName("Activating {B} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent perm = addReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Kjeldoran Dead from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent perm = addReady(player1);
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent survivor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kjeldoran Dead"))
                .findFirst().orElseThrow();
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player) {
        KjeldoranDead card = new KjeldoranDead();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
