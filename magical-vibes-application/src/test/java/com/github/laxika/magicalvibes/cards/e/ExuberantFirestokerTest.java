package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExuberantFirestokerTest extends BaseCardTest {

    // ===== End-step: deal 2 damage to target player or planeswalker =====

    @Test
    @DisplayName("Deals 2 damage to the chosen opponent when controlling a power-5-or-greater creature and accepting")
    void dealsDamageWhenControllingBigCreature() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Any player is a legal target — controller may be chosen")
    void canTargetController() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger without a power-5-or-greater creature")
    void noTriggerWithoutBigCreature() {
        harness.addToBattlefield(player1, new ExuberantFirestoker()); // 1/1 only
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorlessMana() {
        addCreatureReady(player1, new ExuberantFirestoker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Helpers =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, triggers fire
    }
}
