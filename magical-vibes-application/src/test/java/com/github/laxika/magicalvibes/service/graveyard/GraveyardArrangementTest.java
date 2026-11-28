package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.cards.f.FearOfChange;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Necratog;
import com.github.laxika.magicalvibes.cards.s.SoldeviDigger;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathOfGod.class, TomeScour.class, GrizzlyBears.class, HillGiant.class, Forest.class, Necratog.class,
        SoldeviDigger.class, FearOfChange.class})
@DisplayName("Arranging cards put into a graveyard at the same time (CR 404.3)")
class GraveyardArrangementTest extends BaseCardTest {

    @Nested
    @DisplayName("With a card that reads graveyard order in the game")
    class WithReader {

        @Test
        @DisplayName("The owner arranges the creatures a wrath destroyed together")
        void ownerArrangesWrathedCreatures() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            castWrath(List.of(bears, giant), List.of(new Necratog()));

            PendingInteraction.ColorChoice prompt = arrangementPrompt();
            assertThat(prompt).isNotNull();
            assertThat(prompt.playerId()).isEqualTo(player1.getId());
            assertThat(prompt.options()).hasSize(2);

            String bottom = prompt.options().stream().filter(option -> option.endsWith("Hill Giant"))
                    .findFirst().orElseThrow();
            harness.handleListChoice(player1, bottom);

            assertThat(arrangementPrompt()).isNull();
            List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
            assertThat(graveyard.indexOf(giant)).isLessThan(graveyard.indexOf(bears));
        }

        @Test
        @DisplayName("The other order can be chosen too")
        void otherOrderCanBeChosen() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            castWrath(List.of(bears, giant), List.of(new Necratog()));

            String bottom = arrangementPrompt().options().stream().filter(option -> option.endsWith("Grizzly Bears"))
                    .findFirst().orElseThrow();
            harness.handleListChoice(player1, bottom);

            List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
            assertThat(graveyard.indexOf(bears)).isLessThan(graveyard.indexOf(giant));
        }

        @Test
        @DisplayName("Interchangeable copies of one card aren't offered")
        void copiesAreNotOffered() {
            castWrath(List.of(new GrizzlyBears(), new GrizzlyBears()), List.of(new Necratog()));

            assertThat(arrangementPrompt()).isNull();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(2);
        }

        @Test
        @DisplayName("A reader in a sideboard counts, since a wish can fetch it")
        void readerInSideboardCounts() {
            gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(new Necratog())));
            castWrath(List.of(new GrizzlyBears(), new HillGiant()), List.of());

            assertThat(arrangementPrompt()).isNotNull();
        }

        @Test
        @DisplayName("An opponent's reader counts too")
        void opponentsReaderCounts() {
            harness.setHand(player2, List.of(new Necratog()));
            castWrath(List.of(new GrizzlyBears(), new HillGiant()), List.of());

            assertThat(arrangementPrompt()).isNotNull();
        }

        @Test
        @DisplayName("A card that can conjure any creature card counts")
        void catalogGeneratorCounts() {
            castWrath(List.of(new GrizzlyBears(), new HillGiant()), List.of(new FearOfChange()));

            assertThat(arrangementPrompt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Without a reader")
    class WithoutReader {

        @Test
        @DisplayName("No arrangement is offered and the cards go to the graveyard")
        void noPromptWithoutReader() {
            castWrath(List.of(new GrizzlyBears(), new HillGiant()), List.of());

            assertThat(arrangementPrompt()).isNull();
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Hill Giant");
        }
    }

    @Nested
    @DisplayName("Readers of one type's order")
    class TypedReader {

        @Test
        @DisplayName("Only the top creature card is read, so a creature and a land milled together aren't offered")
        void typedReaderSkipsOtherTypes() {
            castTomeScourOnSelf(new Necratog());

            assertThat(arrangementPrompt()).isNull();
        }

        @Test
        @DisplayName("A reader of the top card of any type offers a creature and a land milled together")
        void wholeOrderReaderOffersBoth() {
            castTomeScourOnSelf(new SoldeviDigger());

            assertThat(arrangementPrompt()).isNotNull();
            assertThat(arrangementPrompt().options()).hasSize(2);
        }
    }

    private void castWrath(List<Card> creatures, List<Card> library) {
        creatures.forEach(creature -> harness.addToBattlefield(player1, creature));
        harness.setLibrary(player1, new ArrayList<>(library));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void castTomeScourOnSelf(Card reader) {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new TomeScour(), reader));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
    }

    private PendingInteraction.ColorChoice arrangementPrompt() {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        return choice != null && choice.context() instanceof ChoiceContext.GraveyardArrangement ? choice : null;
    }
}
