package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranDead.class, BalduvianBears.class, Swamp.class})
class KjeldoranDeadTest extends BaseCardTest {

    // ===== ETB: sacrifice a creature =====

    @Test
    @DisplayName("ETB sacrifices Kjeldoran Dead itself when it is the only creature")
    void etbSacrificesItselfWhenOnlyCreature() {
        harness.castFromHand(player1, new KjeldoranDead(), "{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Kjeldoran Dead");
        harness.assertInGraveyard(player1, "Kjeldoran Dead");
    }

    @Test
    @DisplayName("ETB sacrifices a creature rather than a noncreature permanent")
    void etbOnlyConsidersCreaturesForSacrifice() {
        harness.addToBattlefield(player1, new Swamp());

        harness.castFromHand(player1, new KjeldoranDead(), "{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Kjeldoran Dead");
        harness.assertInGraveyard(player1, "Kjeldoran Dead");
        harness.assertOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("ETB lets controller choose which creature to sacrifice; Kjeldoran Dead can be spared")
    void etbControllerChoosesToSacrificeOtherCreature() {
        harness.addToBattlefield(player1, new BalduvianBears());

        harness.castFromHand(player1, new KjeldoranDead(), "{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        Permanent bears = findPermanent(player1, "Balduvian Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Kjeldoran Dead");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    // ===== {B}: Regenerate =====

    @Test
    @DisplayName("Activating {B} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent perm = addCreatureReady(player1, new KjeldoranDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Kjeldoran Dead from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new KjeldoranDead());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        Permanent survivor = findPermanent(player1, "Kjeldoran Dead");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

}
