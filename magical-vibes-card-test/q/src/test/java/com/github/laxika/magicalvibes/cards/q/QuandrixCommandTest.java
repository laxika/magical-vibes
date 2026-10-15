package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mobilization;
import com.github.laxika.magicalvibes.cards.p.ProfessorOnyx;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuandrixCommand.class, Forest.class, GrizzlyBears.class, Shock.class, Spellbook.class,
        Mobilization.class, ProfessorOnyx.class})
class QuandrixCommandTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({"0, false", "0, true", "2, false", "2, true"})
    @DisplayName("Retains the creature target when paired with the graveyard shuffle mode")
    void retainsCreatureTargetWithShuffleMode(int creatureMode, boolean hasGraveyardCard) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new Shock();
        harness.setGraveyard(player2, hasGraveyardCard ? List.of(graveyardCard) : List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{creatureMode, 3}, null,
                List.of(target.getId(), player2.getId()));
        if (hasGraveyardCard) {
            harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        }
        harness.passBothPriorities();

        if (creatureMode == 0) {
            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        } else {
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(hasGraveyardCard ? 2 : 1);
        if (hasGraveyardCard) {
            harness.assertNotInGraveyard(player2, "Shock");
            assertThat(gd.playerDecks.get(player2.getId())).contains(graveyardCard);
        }
    }

    @Test
    @DisplayName("Returns a planeswalker and puts counters on a creature")
    void returnsPlaneswalkerAndBoostsCreature() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ProfessorOnyx());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(planeswalker.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Professor Onyx");
        harness.assertNotOnBattlefield(player2, "Professor Onyx");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters an enchantment spell and boosts a creature")
    void countersEnchantmentAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Mobilization enchantment = new Mobilization();
        harness.setHand(player2, List.of(enchantment));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 2}, enchantment.getId(),
                List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mobilization");
        harness.assertNotOnBattlefield(player2, "Mobilization");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a creature and puts counters on another creature")
    void returnsCreatureAndPutsCountersOnAnotherCreature() {
        Permanent toReturn = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent toBoost = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(toReturn.getId(), toBoost.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(toBoost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a creature before countering an artifact spell")
    void returnsCreatureAndCountersArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Spellbook artifact = new Spellbook();
        harness.setHand(player2, List.of(artifact));
        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 1}, artifact.getId(),
                List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Allows separate modes to target the same creature")
    void allowsSeparateModesToTargetSameCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(target.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters an artifact spell and shuffles up to three cards from a target player's graveyard")
    void countersArtifactSpellAndShufflesGraveyardCards() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Card graveyardFirst = new GrizzlyBears();
        Card graveyardSecond = new Shock();
        Card graveyardThird = new Forest();
        harness.setGraveyard(player1, List.of(graveyardFirst, graveyardSecond, graveyardThird));
        harness.setLibrary(player1, List.of(new Forest()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, spellbook.getId(),
                List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardFirst.getId(), graveyardSecond.getId(),
                graveyardThird.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(graveyardFirst.getId(), graveyardSecond.getId(), graveyardThird.getId());
        harness.assertInGraveyard(player1, "Quandrix Command");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 3);
    }

    @Test
    @DisplayName("The counter mode rejects a creature spell")
    void counterModeRejectsCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, bears.getId(),
                        List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
