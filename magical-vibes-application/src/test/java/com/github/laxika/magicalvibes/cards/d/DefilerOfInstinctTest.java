package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WarTorchGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Defiler of Instinct")
@CardUsed({DefilerOfInstinct.class, WarTorchGoblin.class, Shock.class, GrizzlyBears.class})
class DefilerOfInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a red permanent spell by {R} and deals 1 damage")
    void paysLifeForRedPermanentSpell() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new WarTorchGoblin()));
        harness.setLife(player1, 20);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, null, null, null, List.of(), List.of(), false,
                null, null, List.of(), List.of(), null, null, false, true, null);
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("paying the reduced red mana cost leaves life unchanged and deals 1 damage")
    void paysReducedManaForRedPermanentSpell() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new WarTorchGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("red nonpermanent and nonred permanent spells do not trigger")
    void ignoresNonMatchingSpells() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void chooseTriggerTarget(java.util.UUID targetId) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
