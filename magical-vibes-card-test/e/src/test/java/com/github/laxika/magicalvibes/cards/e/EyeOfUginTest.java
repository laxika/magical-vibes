package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfUgin.class, EmrakulThePromisedEnd.class, GnarlidPack.class,
        LodestoneGolem.class, EverflowingChalice.class})
class EyeOfUginTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless Eldrazi spells you cast cost {2} less")
    void reducesColorlessEldraziSpells() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Emrakul, the Promised End"));
    }

    @Test
    @DisplayName("The reduction does not apply to non-Eldrazi spells")
    void doesNotReduceNonEldraziSpells() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Searches only for colorless creatures and puts the chosen card into hand")
    void searchesForCreatureCard() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.setLibrary(player1, List.of(new LodestoneGolem(), new GnarlidPack(),
                new EverflowingChalice()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Lodestone Golem");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Lodestone Golem");
        harness.assertNotInHand(player1, "Gnarlid Pack");
        harness.assertNotInHand(player1, "Everflowing Chalice");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Gnarlid Pack", "Everflowing Chalice");
    }

    @Test
    void reductionDoesNotApplyToColorlessNonEldrazi() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.setHand(player1, List.of(new LodestoneGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionIsExactlyTwoMana() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsEyeDoesNotReduceYourSpells() {
        harness.addToBattlefield(player2, new EyeOfUgin());
        harness.setHand(player1, List.of(new EmrakulThePromisedEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void searchRequiresSevenMana() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchTapsEyeAndCannotBeActivatedAgainWhileTapped() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.addMana(player1, ManaColor.COLORLESS, 14);
        harness.setLibrary(player1, List.of(new LodestoneGolem()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayFailToFindEvenWhenEligibleCreatureExists() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLibrary(player1, List.of(new LodestoneGolem()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Lodestone Golem");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Lodestone Golem");
    }

    @Test
    void searchResolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new EyeOfUgin());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
