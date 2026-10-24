package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GerrardsWisdom;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowGhoul.class, BenalishInfantry.class, GerrardsWisdom.class})
class BarrowGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling the top creature card of your graveyard keeps Barrow Ghoul")
    void payingExilesTopCreatureCard() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Declining the exile sacrifices Barrow Ghoul")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("With no creature card in the graveyard, the Ghoul's death replacement is chosen before it is sacrificed")
    void noCreatureCardAsksForTheDeathReplacementBeforeTheGhoulIsSacrificed() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new BarrowGhoul());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(ghoul.getId());
        harness.setGraveyard(player1, List.of(new GerrardsWisdom()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.inDepartureBatch()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ghoul);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(ghoul.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ghoul);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    @Test
    @DisplayName("With no creature card in the graveyard the Ghoul is sacrificed without a prompt")
    void noCreatureCardSacrifices() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new GerrardsWisdom()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        harness.assertInGraveyard(player1, "Gerrard's Wisdom");
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped, not blockers")
    void skipsNoncreatureCardsAboveIt() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new GerrardsWisdom()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Gerrard's Wisdom");
    }

    @Test
    @DisplayName("Only one of two Ghouls can exile the same eligible creature card")
    void oneCreatureCardPaysForOnlyOneGhoul() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Barrow Ghoul")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Barrow Ghoul");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Can still exile a creature after the Ghoul leaves the battlefield")
    void canPayAfterSourceLeavesBattlefield() {
        var ghoul = harness.addToBattlefieldAndReturn(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, ghoul));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Payment exiles only the topmost creature, leaving older creatures in place")
    void exilesOnlyTopmostCreature() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of(
                new BarrowGhoul(), new BenalishInfantry(), new GerrardsWisdom()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Barrow Ghoul");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Barrow Ghoul", "Gerrard's Wisdom");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName())
                .containsExactly("Benalish Infantry");
    }

    @Test
    @DisplayName("An opponent's creature card cannot pay for an empty controller graveyard")
    void cannotPayWithOpponentsGraveyard() {
        harness.addToBattlefield(player1, new BarrowGhoul());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new BenalishInfantry()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barrow Ghoul");
        harness.assertInGraveyard(player1, "Barrow Ghoul");
        harness.assertInGraveyard(player2, "Benalish Infantry");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
