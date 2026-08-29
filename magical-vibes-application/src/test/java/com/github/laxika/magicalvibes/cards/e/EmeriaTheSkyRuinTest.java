package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmeriaTheSkyRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new EmeriaTheSkyRuin()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Emeria, the Sky Ruin").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds one white mana")
    void addsWhiteMana() {
        Permanent emeria = harness.addToBattlefieldAndReturn(player1, new EmeriaTheSkyRuin());
        emeria.setSummoningSick(false);
        emeria.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("With seven Plains, returns an optional target creature from the graveyard")
    void returnsTargetCreatureWithSevenPlains() {
        addEmeriaWithPlains(7);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional target can be declined")
    void canDeclineTargetCreature() {
        addEmeriaWithPlains(7);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        var bears = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger with fewer than seven Plains")
    void doesNotTriggerWithFewerThanSevenPlains() {
        addEmeriaWithPlains(6);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards are legal targets")
    void ignoresNonCreatureCards() {
        addEmeriaWithPlains(7);
        harness.setGraveyard(player1, List.of(new Plains()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Plains");
    }

    private void addEmeriaWithPlains(int plains) {
        harness.addToBattlefield(player1, new EmeriaTheSkyRuin());
        for (int i = 0; i < plains; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
    }
}
