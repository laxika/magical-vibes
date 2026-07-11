package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrchardWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the entering Treefolk's toughness when accepting")
    void gainsLifeWhenTreefolkEntersAccept() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new BoskBanneret())); // 1/3 Treefolk
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Orchard Warden triggers
        harness.passBothPriorities(); // resolve may → prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23); // +3 = Bosk Banneret's toughness
    }

    @Test
    @DisplayName("No life gain when declining the may")
    void noLifeGainWhenDeclining() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new BoskBanneret()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when a non-Treefolk creature enters")
    void doesNotTriggerForNonTreefolk() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears())); // Bear, not Treefolk
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when Orchard Warden itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new OrchardWarden()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }
}
