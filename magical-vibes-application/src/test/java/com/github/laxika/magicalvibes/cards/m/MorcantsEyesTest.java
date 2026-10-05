package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EclipsedElf;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GanglyStompling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorcantsEyes.class, GrizzlyBears.class, Card.class, EclipsedElf.class, EvolvingWilds.class, GanglyStompling.class})
class MorcantsEyesTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of your upkeep")
    void surveilsAtUpkeep() {
        harness.addToBattlefield(player1, new MorcantsEyes());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing creates one 2/2 Elf token per Elf card in the graveyard")
    void createsTokensPerElfCardAndSacrifices() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setGraveyard(player1, List.of(elfCard("Elvish Visionary"), elfCard("Imperious Perfect"), nonElfCard()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        Permanent eyes = findPermanent(player1, "Morcant's Eyes");
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(eyes);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf"))
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELF);
                });

        harness.assertNotOnBattlefield(player1, "Morcant's Eyes");
        harness.assertInGraveyard(player1, "Morcant's Eyes");
    }

    @Test
    void canKeepSurveilledCardOnTop() {
        harness.addToBattlefield(player1, new MorcantsEyes());
        Card top = new EvolvingWilds();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertNotInGraveyard(player1, "Evolving Wilds");
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new MorcantsEyes());
        Card top = new EvolvingWilds();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void surveilsEmptyLibraryWithoutAChoice() {
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void countsSacrificedEnchantmentAndChangelingButNotOpponentsElves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setGraveyard(player1, List.of(new GanglyStompling(), new EvolvingWilds()));
        harness.setGraveyard(player2, List.of(new EclipsedElf()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Morcant's Eyes");
        assertThat(findPermanents(player1, "Elf")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf")).hasSize(2);
        assertThat(findPermanents(player2, "Elf")).isEmpty();
    }

    @Test
    void countsGraveyardAtResolutionRatherThanActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setGraveyard(player1, List.of(new EclipsedElf()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.setGraveyard(player1, List.of(new EvolvingWilds()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf")).isEmpty();
    }

    @Test
    void emptyStartingGraveyardStillCreatesOneToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf")).hasSize(1);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Morcant's Eyes");
    }

    @Test
    void cannotPayActivationWithOnlyOneGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Morcant's Eyes");
    }
    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Morcant's Eyes");
    }

    @Test
    void cannotActivateWithAnAbilityOnTheStack() {
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Morcant's Eyes");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }
    private static Card elfCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.ELF));
        return card;
    }

    private static Card nonElfCard() {
        Card card = new Card();
        card.setName("Grizzly Bears");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }
}
