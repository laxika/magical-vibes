package com.github.laxika.magicalvibes.cards.l;

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
        ManifoldMouse.class, Mockingbird.class, Opt.class, ZoralineCosmosCaller.class})
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
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new Opt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
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
