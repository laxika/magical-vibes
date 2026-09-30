package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenSubstitution.class, GrizzlyBears.class, LavaAxe.class})
class SuddenSubstitutionTest extends BaseCardTest {

    @Test
    void exchangesControlAndRetargetsTheSpellWithItsNewController() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new SuddenSubstitution()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, List.of(lavaAxe.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void decliningRetargetingLeavesTheOriginalTargetInPlace() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new SuddenSubstitution()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, List.of(lavaAxe.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetACreatureSpell() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(creatureSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new SuddenSubstitution()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(
                player2, 0, List.of(creatureSpell.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
