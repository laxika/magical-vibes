package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IkraShidiqiTheUsurper;
import com.github.laxika.magicalvibes.cards.m.Mirrorweave;
import com.github.laxika.magicalvibes.cards.t.ThrasiosTritonHero;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StingingStudy.class, IkraShidiqiTheUsurper.class, Forest.class,
        ThrasiosTritonHero.class, Mirrorweave.class, SolemnSimulacrum.class})
class StingingStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and loses life equal to the mana value of a commander in the command zone")
    void usesCommanderInCommandZone() {
        Card commander = new IkraShidiqiTheUsurper();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Counts a commander you own even when an opponent controls it")
    void usesOwnedCommanderOnBattlefield() {
        Card commander = new IkraShidiqiTheUsurper();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Prompts to choose which owned commander determines X")
    void choosesAmongMultipleCommanders() {
        Card lowValueCommander = new ThrasiosTritonHero();
        Card highValueCommander = new IkraShidiqiTheUsurper();
        gd.playerCommanders.put(player1.getId(), List.of(lowValueCommander, highValueCommander));
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(lowValueCommander, highValueCommander)));
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.StingingStudyCommanderChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(highValueCommander.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does nothing when no commander is designated")
    void noCommanderDrawsNothingAndLosesNoLife() {
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Stinging Study");
    }

    @Test
    @DisplayName("Ignores owned commanders in the graveyard and exile")
    void ignoresCommandersOutsideEligibleZones() {
        Card first = new IkraShidiqiTheUsurper();
        Card second = new ThrasiosTritonHero();
        gd.playerCommanders.put(player1.getId(), List.of(first, second));
        harness.setGraveyard(player1, List.of(first));
        harness.setExile(player1, List.of(second));
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Ignores a commander owned by an opponent even when you control it")
    void ignoresOpponentOwnedCommander() {
        Card commander = new IkraShidiqiTheUsurper();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A face-down commander has mana value zero")
    void faceDownCommanderDrawsNothingAndLosesNoLife() {
        Card commander = new IkraShidiqiTheUsurper();
        gd.makeCommander(player1.getId(), commander);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, commander);
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Determines commander eligibility when the spell resolves")
    void ignoresCommanderThatLeftBeforeResolution() {
        Card commander = new IkraShidiqiTheUsurper();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        prepareStudy();
        harness.castInstant(player1, 0);

        gd.playerCommandZones.get(player1.getId()).clear();
        harness.setHand(player1, List.of(commander));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(commander);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can choose the smaller of two commanders across battlefield and command zone")
    void canChooseLowerManaValueCommander() {
        Card lowValueCommander = new ThrasiosTritonHero();
        Card highValueCommander = new IkraShidiqiTheUsurper();
        gd.playerCommanders.put(player1.getId(), List.of(lowValueCommander, highValueCommander));
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(lowValueCommander)));
        harness.addToBattlefield(player1, highValueCommander);
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(lowValueCommander.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Uses the copied mana value of a commander that became another creature")
    void usesManaValueOfCopiedCommander() {
        Card commander = new IkraShidiqiTheUsurper();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        copyCreaturesIntoSolemnSimulacrum();
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("A copied commander remains a legal choice among multiple commanders")
    void canChooseCopiedCommander() {
        Card commander = new IkraShidiqiTheUsurper();
        Card partner = new ThrasiosTritonHero();
        gd.playerCommanders.put(player1.getId(), List.of(commander, partner));
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(partner)));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, commander);
        copyCreaturesIntoSolemnSimulacrum();
        prepareStudy();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getCard().getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 16);
    }

    private void copyCreaturesIntoSolemnSimulacrum() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolemnSimulacrum());
        harness.setHand(player2, List.of(new Mirrorweave()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void prepareStudy() {
        harness.setHand(player1, List.of(new StingingStudy()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        addStudyMana();
    }

    private void addStudyMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
