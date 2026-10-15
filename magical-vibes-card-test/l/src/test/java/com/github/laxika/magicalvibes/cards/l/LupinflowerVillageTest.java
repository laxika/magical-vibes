package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrumbAndGetIt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntrepidRabbit;
import com.github.laxika.magicalvibes.cards.m.ManifoldMouse;
import com.github.laxika.magicalvibes.cards.m.Mockingbird;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.z.ZoralineCosmosCaller;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LupinflowerVillage.class, GrizzlyBears.class, IntrepidRabbit.class,
        ManifoldMouse.class, Mockingbird.class, Opt.class, ZoralineCosmosCaller.class, CrumbAndGetIt.class})
class LupinflowerVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsWhiteManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.WHITE))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyWhiteManaCannotCastNoncreatureSpells() {
        addVillage();
        var rabbit = harness.addToBattlefieldAndReturn(player1, new IntrepidRabbit());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new CrumbAndGetIt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, rabbit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, rabbit.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.WHITE))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyWhiteManaCanPayForCreatureSpell() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new IntrepidRabbit()));

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.WHITE))
                .isZero();
    }

    @Test
    void sacrificeIsPaidBeforeAbilityResolvesAndOnlyTopSixAreLookedAt() {
        harness.setHand(player1, List.of());
        Card rabbit = new IntrepidRabbit();
        Card first = new CrumbAndGetIt();
        Card second = new CrumbAndGetIt();
        Card third = new CrumbAndGetIt();
        Card fourth = new CrumbAndGetIt();
        Card fifth = new CrumbAndGetIt();
        Card seventh = new ManifoldMouse();
        Card eighth = new Mockingbird();
        setLibrary(rabbit, first, second, third, fourth, fifth, seventh, eighth);
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        harness.assertInGraveyard(player1, "Lupinflower Village");
        harness.assertNotOnBattlefield(player1, "Lupinflower Village");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(rabbit, first, second, third, fourth, fifth, seventh, eighth);

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(rabbit.getId());
        harness.handleMultipleCardsChosen(player1, List.of(rabbit.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(rabbit);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(seventh, eighth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 7))
                .containsExactlyInAnyOrder(first, second, third, fourth, fifth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotRevealMoreThanOneMatchingCard() {
        harness.setHand(player1, List.of());
        Card rabbit = new IntrepidRabbit();
        Card mouse = new ManifoldMouse();
        setLibrary(rabbit, mouse);
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(rabbit.getId(), mouse.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(rabbit.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(rabbit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mouse);
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        harness.setHand(player1, List.of());
        setLibrary();
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lupinflower Village");
    }

    @Test
    void mayRevealOneMatchingValleyCreatureAndBottomsTheRestRandomly() {
        Card bat = new ZoralineCosmosCaller();
        Card bird = new Mockingbird();
        Card mouse = new ManifoldMouse();
        Card rabbit = new IntrepidRabbit();
        Card bear = new GrizzlyBears();
        Card opt = new Opt();
        setLibrary(bat, bird, mouse, rabbit, bear, opt);
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bat.getId(), bird.getId(), mouse.getId(), rabbit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(mouse.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(mouse);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(bat, bird, rabbit, bear, opt);
        harness.assertInGraveyard(player1, "Lupinflower Village");
    }

    @Test
    void decliningTheRevealPutsAllLookedAtCardsOnTheBottom() {
        Card rabbit = new IntrepidRabbit();
        Card bear = new GrizzlyBears();
        Card opt = new Opt();
        setLibrary(rabbit, bear, opt);
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(rabbit);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(rabbit, bear, opt);
        harness.assertInGraveyard(player1, "Lupinflower Village");
    }

    @Test
    void noMatchingCardIsPutOnTheBottomWithoutAChoice() {
        Card bear = new GrizzlyBears();
        Card opt = new Opt();
        setLibrary(bear, opt);
        addVillage();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bear, opt);
        harness.assertInGraveyard(player1, "Lupinflower Village");
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new LupinflowerVillage());
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
