package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishSoultiller.class, ElvishWarrior.class, GrizzlyBears.class, AvianChangeling.class,
        WrathOfGod.class, ElvishPromenade.class, Conspiracy.class})
class ElvishSoultillerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it shuffles only creature cards of the chosen type from its graveyard")
    void shufflesOnlyChosenTypeFromOwnGraveyard() {
        Card soultiller = new ElvishSoultiller();
        Card elf = new ElvishWarrior();
        Card bear = new GrizzlyBears();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(elf, bear));

        destroySoultiller();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(soultiller.getId(), elf.getId())
                .doesNotContain(bear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bear.getId())
                .doesNotContain(elf.getId(), soultiller.getId());
    }

    @Test
    @DisplayName("Changeling creature cards match the chosen type and an opponent's graveyard is untouched")
    void changelingMatchesChosenTypeAndOpponentGraveyardStays() {
        Card changeling = new AvianChangeling();
        Card opponentBear = new GrizzlyBears();
        harness.addToBattlefield(player1, new ElvishSoultiller());
        harness.setGraveyard(player1, List.of(changeling));
        harness.setGraveyard(player2, List.of(opponentBear));

        destroySoultiller();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(changeling.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(opponentBear.getId());
    }

    @Test
    @DisplayName("A noncreature card with the chosen creature subtype stays in the graveyard")
    void doesNotShuffleNoncreatureCardOfChosenType() {
        Card soultiller = new ElvishSoultiller();
        Card elfKindred = new ElvishPromenade();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(elfKindred));

        destroySoultiller();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(elfKindred.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(elfKindred.getId());
    }

    @Test
    @DisplayName("Choosing Mutant returns Soultiller itself without returning other Elves")
    void shufflesSoultillerForItsOtherCreatureType() {
        Card soultiller = new ElvishSoultiller();
        Card elf = new ElvishWarrior();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(elf));

        destroySoultiller();
        harness.handleListChoice(player1, "MUTANT");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(soultiller.getId())
                .doesNotContain(elf.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(elf.getId())
                .doesNotContain(soultiller.getId());
    }

    @Test
    @DisplayName("A type with no matching cards leaves the graveyard intact")
    void choosingTypeWithoutMatchesLeavesGraveyardIntact() {
        Card soultiller = new ElvishSoultiller();
        Card bear = new GrizzlyBears();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(bear));
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        destroySoultiller();
        harness.handleListChoice(player1, "DRAGON");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(soultiller.getId(), bear.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses and shuffles their graveyard even when an opponent owns Soultiller")
    void usesControllerGraveyardRatherThanOwnerGraveyard() {
        Card soultiller = new ElvishSoultiller();
        soultiller.setOwnerId(player2.getId());
        Card elf = new ElvishWarrior();
        Card opponentElf = new ElvishWarrior();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(elf));
        harness.setGraveyard(player2, List.of(opponentElf));

        destroySoultiller();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(elf.getId())
                .doesNotContain(soultiller.getId(), opponentElf.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(soultiller.getId(), opponentElf.getId());
    }

    @Test
    @DisplayName("Conspiracy's chosen type applies to creature cards being shuffled from the graveyard")
    void shufflesCardsWithCreatureTypeGrantedByConspiracy() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        Card soultiller = new ElvishSoultiller();
        Card bear = new GrizzlyBears();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(bear));

        destroySoultiller();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(soultiller.getId(), bear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(soultiller.getId(), bear.getId());
    }

    @Test
    @DisplayName("Creature types replaced by Conspiracy do not match the chosen type")
    void doesNotShuffleCardsWhoseCreatureTypeWasReplaced() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        Card bear = new GrizzlyBears();
        Card changeling = new AvianChangeling();
        harness.addToBattlefield(player1, new ElvishSoultiller());
        harness.setGraveyard(player1, List.of(bear, changeling));

        destroySoultiller();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(bear.getId(), changeling.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bear.getId(), changeling.getId());
    }

    private void destroySoultiller() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
