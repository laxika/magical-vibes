package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HenchfiendOfUkor.class})
class HenchfiendOfUkorTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability can be paid with red mana")
    void activatedAbilityCanBePaidWithRedMana() {
        Permanent henchfiend = addReadyHenchfiend(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(henchfiend.getEffectivePower()).isEqualTo(4);
        assertThat(henchfiend.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability can be paid with black mana")
    void activatedAbilityCanBePaidWithBlackMana() {
        Permanent henchfiend = addReadyHenchfiend(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(henchfiend.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability boost wears off at end of turn")
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent henchfiend = addReadyHenchfiend(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(henchfiend.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining echo sacrifices Henchfiend of Ukor")
    void decliningEchoSacrificesHenchfiendOfUkor() {
        castAndResolveHenchfiend();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Henchfiend of Ukor");
        harness.assertInGraveyard(player1, "Henchfiend of Ukor");
    }

    @Test
    @DisplayName("Paying echo keeps Henchfiend of Ukor and echo does not trigger again")
    void payingEchoKeepsHenchfiendOfUkorAndIsOneShot() {
        castAndResolveHenchfiend();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Henchfiend of Ukor");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Henchfiend of Ukor");
    }

    private Permanent addReadyHenchfiend(Player player) {
        Permanent perm = new Permanent(new HenchfiendOfUkor());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castAndResolveHenchfiend() {
        harness.setHand(player1, List.of(new HenchfiendOfUkor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Henchfiend of Ukor");
    }
}
