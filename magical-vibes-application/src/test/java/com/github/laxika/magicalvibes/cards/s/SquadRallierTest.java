package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquadRallier.class, LlanowarElves.class, DwynensElite.class, Juggernaut.class, BurstLightning.class, Plains.class})
class SquadRallierTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only creature cards with power 2 or less from the top four")
    void offersSmallCreatureCards() {
        addCreatureReady(player1, new SquadRallier());
        Card smallCreature = new LlanowarElves();
        Card powerTwoCreature = new DwynensElite();
        harness.setLibrary(player1, List.of(smallCreature, new Juggernaut(), new BurstLightning(), powerTwoCreature));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactlyInAnyOrder(smallCreature.getId(), powerTwoCreature.getId());
    }

    @Test
    @DisplayName("Putting an eligible creature into hand sends the rest to the bottom")
    void chosenCreatureGoesToHand() {
        addCreatureReady(player1, new SquadRallier());
        Card smallCreature = new LlanowarElves();
        harness.setLibrary(player1, List.of(smallCreature, new Juggernaut(), new BurstLightning(), new DwynensElite()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(smallCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(smallCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With no eligible creature, all four cards go to the bottom")
    void noEligibleCreatureGoesToBottom() {
        addCreatureReady(player1, new SquadRallier());
        harness.setLibrary(player1, List.of(new Juggernaut(), new BurstLightning(), new Plains(), new BurstLightning()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    void mayDeclineEligibleCreatureAndOnlyBottomTopFour() {
        addCreatureReady(player1, new SquadRallier());
        Card elf = new LlanowarElves();
        Card elite = new DwynensElite();
        Card spell = new BurstLightning();
        Card giant = new Juggernaut();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(elf, elite, spell, giant, untouched));
        harness.setHand(player1, List.of());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(elf, elite, spell, giant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesFromShortLibrary() {
        addCreatureReady(player1, new SquadRallier());
        Card elf = new LlanowarElves();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(elf, land));
        harness.setHand(player1, List.of());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseTwoCreaturesOrCreatureBelowTopFour() {
        addCreatureReady(player1, new SquadRallier());
        Card elf = new LlanowarElves();
        Card elite = new DwynensElite();
        Card spell = new BurstLightning();
        Card land = new Plains();
        Card deeperCreature = new LlanowarElves();
        harness.setLibrary(player1, List.of(elf, elite, spell, land, deeperCreature));
        harness.setHand(player1, List.of());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(elf.getId(), elite.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(deeperCreature.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(elite.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elite);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(deeperCreature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(elf, spell, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutChoice() {
        addCreatureReady(player1, new SquadRallier());
        harness.setLibrary(player1, List.of());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
