package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinisterConcoction.class, Forest.class, GrizzlyBears.class})
class SinisterConcoctionTest extends BaseCardTest {

    @Test
    void paysAllCostsAndDestroysTargetCreature() {
        Permanent concoction = harness.addToBattlefieldAndReturn(player1, new SinisterConcoction());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(concoction), null, targetId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Sinister Concoction");
        harness.assertInGraveyard(player1, "Sinister Concoction");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void rejectsNonCreatureTarget() {
        Permanent concoction = harness.addToBattlefieldAndReturn(player1, new SinisterConcoction());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(concoction),
                null,
                forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canDiscardALandAndDestroyOwnCreatureWithCostsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new SinisterConcoction());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sinister Concoction");
        harness.assertNotOnBattlefield(player1, "Sinister Concoction");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))).hasSize(2);
    }

    @Test
    void cannotActivateWithEmptyLibraryAndDoesNotPayOtherCosts() {
        harness.addToBattlefield(player1, new SinisterConcoction());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Sinister Concoction");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Sinister Concoction");
    }

    @Test
    void cannotActivateWithoutACardToDiscardAndDoesNotPayOtherCosts() {
        harness.addToBattlefield(player1, new SinisterConcoction());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Sinister Concoction");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Sinister Concoction");
    }
}
