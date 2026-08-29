package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClashOfRealitiesTest extends BaseCardTest {

    @Test
    @DisplayName("An entering Spirit may deal 3 damage to a non-Spirit creature")
    void enteringSpiritShootsNonSpirit() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An entering non-Spirit creature may deal 3 damage to a Spirit")
    void enteringNonSpiritShootsSpirit() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfFalseHope());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(kami.getId());

        harness.handlePermanentChosen(player1, kami.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Kami of False Hope");
    }

    @Test
    @DisplayName("Declining the granted trigger deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Spirit entering with no non-Spirit creature around gets no trigger")
    void spiritWithoutLegalTargetGetsNoTrigger() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        harness.addToBattlefield(player2, new KamiOfFalseHope());

        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
