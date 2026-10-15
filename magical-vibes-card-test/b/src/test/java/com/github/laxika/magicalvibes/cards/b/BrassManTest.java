package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassMan.class})
class BrassManTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Brass Man does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent brassMan = addBrassMan(player1, true);

        harness.performUntapStep(player1);

        assertThat(brassMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} during upkeep untaps Brass Man")
    void payingOneUntapsBrassMan() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brassMan.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Brass Man tapped")
    void decliningLeavesBrassManTapped() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(brassMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Brass Man stays tapped when its controller cannot pay {1}")
    void cannotPayOneLeavesBrassManTapped() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brassMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Brass Man does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(brassMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana can pay the generic upkeep cost")
    void coloredManaPaysUpkeepCost() {
        Permanent brassMan = addBrassMan(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brassMan.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An untapped Brass Man still offers the upkeep payment")
    void untappedBrassManStillTriggers() {
        Permanent brassMan = addBrassMan(player1, false);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brassMan.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A departed Brass Man's trigger cannot untap a different Brass Man")
    void departedSourceDoesNotUntapReplacement() {
        Permanent original = addBrassMan(player1, true);
        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());
        Permanent replacement = addBrassMan(player1, true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(replacement.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addBrassMan(Player player, boolean tapped) {
        Permanent perm = addCreatureReady(player, new BrassMan());
        if (tapped) {
            perm.tap();
        }
        return perm;
    }

}
