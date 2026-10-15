package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriaTheSkyRuin.class, StoneworkPuma.class, Plains.class})
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
        StoneworkPuma puma = new StoneworkPuma();
        harness.setGraveyard(player1, List.of(puma));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(puma.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Stonework Puma");
        harness.assertNotInGraveyard(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("The optional target can be declined")
    void canDeclineTargetCreature() {
        addEmeriaWithPlains(7);
        harness.setGraveyard(player1, List.of(new StoneworkPuma()));

        advanceToUpkeep(player1);

        var puma = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(puma.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Stonework Puma");
        harness.assertNotOnBattlefield(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("Does not trigger with fewer than seven Plains")
    void doesNotTriggerWithFewerThanSevenPlains() {
        addEmeriaWithPlains(6);
        harness.setGraveyard(player1, List.of(new StoneworkPuma()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Stonework Puma");
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

    @Test
    @DisplayName("Losing the seventh Plains before resolution prevents the return")
    void rechecksPlainsCountAtResolution() {
        addEmeriaWithPlains(7);
        StoneworkPuma puma = new StoneworkPuma();
        harness.setGraveyard(player1, List.of(puma));
        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(puma.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Plains"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Stonework Puma");
        harness.assertNotOnBattlefield(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        addEmeriaWithPlains(7);
        harness.setGraveyard(player1, List.of(new StoneworkPuma()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("Opponent's Plains do not count toward the threshold")
    void doesNotCountOpponentsPlains() {
        addEmeriaWithPlains(6);
        harness.addToBattlefield(player2, new Plains());
        harness.setGraveyard(player1, List.of(new StoneworkPuma()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("Creatures in the opponent's graveyard are not legal targets")
    void doesNotTargetOpponentsGraveyard() {
        addEmeriaWithPlains(7);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new StoneworkPuma()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Stonework Puma");
        harness.assertNotOnBattlefield(player1, "Stonework Puma");
    }

    @Test
    @DisplayName("Removing the graveyard target before resolution prevents the return")
    void doesNotReturnTargetThatLeftGraveyard() {
        addEmeriaWithPlains(7);
        StoneworkPuma puma = new StoneworkPuma();
        harness.setGraveyard(player1, List.of(puma));
        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(puma.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(puma));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Stonework Puma");
        assertThat(gd.findExiledCard(puma.getId())).isNotNull();
    }

    @Test
    @DisplayName("The ability resolves after Emeria leaves the battlefield")
    void returnsCreatureAfterSourceLeavesBattlefield() {
        addEmeriaWithPlains(7);
        StoneworkPuma puma = new StoneworkPuma();
        harness.setGraveyard(player1, List.of(puma));
        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(puma.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Emeria, the Sky Ruin"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Stonework Puma");
        harness.assertNotInGraveyard(player1, "Stonework Puma");
    }

    private void addEmeriaWithPlains(int plains) {
        harness.addToBattlefield(player1, new EmeriaTheSkyRuin());
        for (int i = 0; i < plains; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
    }
}
