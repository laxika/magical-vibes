package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronFistPulverizer.class, GrizzlyBears.class})
class IronFistPulverizerTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell triggers damage to an opponent and scry 1")
    void secondSpellDamagesOpponentAndScries() {
        harness.setHand(player1, List.of(new IronFistPulverizer(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new IronFistPulverizer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("The first spell each turn does not trigger the ability")
    void firstSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new IronFistPulverizer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({IronFistPulverizer.class, GrizzlyBears.class})
    @DisplayName("Entering as the second spell does not trigger its own ability")
    void enteringAsSecondSpellDoesNotTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new IronFistPulverizer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed(IronFistPulverizer.class)
    @DisplayName("Scry can bottom the top card and the third spell does not trigger again")
    void thirdSpellDoesNotTriggerAndScryCanBottom() {
        harness.setHand(player1, List.of(
                new IronFistPulverizer(), new IronFistPulverizer(), new IronFistPulverizer()));
        IronFistPulverizer top = new IronFistPulverizer();
        IronFistPulverizer next = new IronFistPulverizer();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
