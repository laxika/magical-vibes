package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EfreetWeaponmasterTest extends BaseCardTest {

    @Test
    void entersAndBoostsAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        addNormalManaCost();

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void turningFaceUpPromptsForAnotherCreatureYouControlAndBoostsIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EfreetWeaponmaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent efreet = findPermanent(player1, "Efreet Weaponmaster");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(efreet));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(efreet.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(efreet.isFaceDown()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    private void addNormalManaCost() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
