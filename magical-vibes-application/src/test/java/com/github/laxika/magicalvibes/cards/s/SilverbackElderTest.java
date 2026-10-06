package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineBinding;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverbackElder.class, AngelsFeather.class, Forest.class, GrizzlyBears.class,
        LeylineBinding.class, EssenceScatter.class})
class SilverbackElderTest extends BaseCardTest {

    private static final String DESTROY = "Destroy target artifact or enchantment.";
    private static final String LAND = "Look at the top five cards of your library. You may put a land card from among them onto the battlefield tapped. Put the rest on the bottom of your library in a random order.";
    private static final String LIFE = "You gain 4 life.";

    @Test
    @DisplayName("The destroy mode destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, DESTROY);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("The destroy mode cannot target a creature")
    void destroyModeRejectsCreatureTarget() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, DESTROY);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The land mode puts a chosen land from the top five onto the battlefield tapped")
    void putsLandOntoBattlefieldTapped() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.selectedToBattlefieldTapped()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .allMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("The life mode gains four life")
    void gainsLife() {
        harness.addToBattlefield(player1, new SilverbackElder());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LIFE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("The destroy mode can destroy an enchantment controlled by its controller")
    void destroysOwnEnchantment() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new LeylineBinding());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, DESTROY);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leyline Binding");
        harness.assertInGraveyard(player1, "Leyline Binding");
    }

    @Test
    @DisplayName("The trigger resolves before the creature spell and does not target for the life mode")
    void gainsLifeBeforeCreatureResolvesWithoutOtherPermanents() {
        harness.addToBattlefield(player1, new SilverbackElder());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LIFE);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 10);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Silverback Elder")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SilverbackElder());
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new AngelsFeather(), "{2}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel's Feather");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Silverback Elder does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new SilverbackElder(), "{2}{G}{G}{G}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silverback Elder");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger Silverback Elder")
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player2, new SilverbackElder());
        harness.setLife(player2, 10);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land mode may decline an available land and preserves the unseen library top")
    void mayDeclineLandAndBottomsOnlyTopFive() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Card forest = new Forest();
        List<Card> topFive = List.of(forest, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        Card sixth = new Forest();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), sixth));
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Forest");
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.getFirst()).isSameAs(sixth);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The land mode works with fewer than five cards in the library")
    void choosesLandFromShortLibrary() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Card forest = new Forest();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(other, forest));
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The land mode puts all looked-at cards on the bottom when there are no lands")
    void noLandInTopFiveDoesNotChooseLandBelowThem() {
        harness.addToBattlefield(player1, new SilverbackElder());
        List<Card> topFive = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        Card sixth = new Forest();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), sixth));
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.getFirst()).isSameAs(sixth);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The land mode does nothing with an empty library")
    void emptyLibraryDoesNotRequestSelection() {
        harness.addToBattlefield(player1, new SilverbackElder());
        harness.setLibrary(player1, List.of());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The trigger still resolves when the creature spell is countered")
    void triggerSurvivesCounteredCreatureSpell() {
        harness.addToBattlefield(player1, new SilverbackElder());
        harness.setLife(player1, 10);
        Card creature = new GrizzlyBears();
        harness.castFromHand(player1, creature, "{1}{G}");
        harness.handleListChoice(player1, LIFE);

        harness.setHand(player2, List.of(new EssenceScatter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same mode may be selected on successive creature casts in the same turn")
    void canRepeatLifeModeInSameTurn() {
        harness.addToBattlefield(player1, new SilverbackElder());
        harness.setLife(player1, 10);
        castCreatureAndResolveTrigger();
        harness.handleListChoice(player1, LIFE);
        harness.passBothPriorities();
        harness.passBothPriorities();

        castCreatureAndResolveTrigger();
        harness.handleListChoice(player1, LIFE);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The land mode cannot put two lands onto the battlefield")
    void landModeAllowsOnlyOneLand() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        castCreatureAndResolveTrigger();
        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger Silverback Elder")
    void creatureEnteringWithoutCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SilverbackElder());
        harness.setLife(player1, 10);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castCreatureAndResolveTrigger() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
    }
}
