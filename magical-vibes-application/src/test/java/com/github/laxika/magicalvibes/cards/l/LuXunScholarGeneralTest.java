package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuXunScholarGeneral.class, ShuCavalry.class, ShuFootSoldiers.class, HermeticStudy.class})
class LuXunScholarGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to a player presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        addCreatureReady(player1, new LuXunScholarGeneral());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw draws a card")
    void acceptingMayDrawsCard() {
        addCreatureReady(player1, new LuXunScholarGeneral());

        declareAttackers(List.of(0));
        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw does not draw a card")
    void decliningMayDoesNotDraw() {
        addCreatureReady(player1, new LuXunScholarGeneral());

        declareAttackers(List.of(0));
        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when blocked and no damage reaches the player")
    void noTriggerWhenBlocked() {
        addCreatureReady(player1, new LuXunScholarGeneral());
        addCreatureReady(player2, new ShuCavalry());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender takes combat damage regardless of the may choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LuXunScholarGeneral());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Lu Xun is 1/3, deals 1 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A creature without horsemanship cannot block Lu Xun")
    void creatureWithoutHorsemanshipCannotBlock() {
        addCreatureReady(player1, new LuXunScholarGeneral());
        addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Noncombat damage to an opponent presents the may-draw choice")
    void noncombatDamageToOpponentPresentsMayChoice() {
        Permanent luXun = addCreatureReady(player1, new LuXunScholarGeneral());
        attachHermeticStudy(luXun);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw after noncombat damage draws a card")
    void acceptingNoncombatMayDrawsCard() {
        Permanent luXun = addCreatureReady(player1, new LuXunScholarGeneral());
        attachHermeticStudy(luXun);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Damage to Lu Xun's controller does not present the may-draw choice")
    void damageToControllerDoesNotTriggerMayDraw() {
        harness.setLife(player1, 20);
        Permanent luXun = addCreatureReady(player1, new LuXunScholarGeneral());
        attachHermeticStudy(luXun);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void attachHermeticStudy(Permanent creature) {
        Permanent aura = new Permanent(new HermeticStudy());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
