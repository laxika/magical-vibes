package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skinthinner.class, FugitiveWizard.class, Island.class})
class SkinthinnerTest extends BaseCardTest {

    @Test
    void turningFaceUpDestroysTargetNonblackCreatureWithoutRegeneration() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        wizard.setRegenerationShield(1);
        Permanent skinthinner = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(skinthinner));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(wizard.getId());
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();

        assertThat(skinthinner.isFaceDown()).isFalse();
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    void cannotChooseBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new Skinthinner());
        Permanent skinthinner = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(skinthinner));

        harness.passBothPriorities();

        assertThat(skinthinner.isFaceDown()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blackCreature);
    }

    @Test
    void cannotChooseNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent skinthinner = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(skinthinner));

        harness.passBothPriorities();

        assertThat(skinthinner.isFaceDown()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(island);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new Skinthinner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Skinthinner");
    }
}
