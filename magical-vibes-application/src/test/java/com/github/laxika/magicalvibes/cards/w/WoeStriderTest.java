package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HavengulLich;
import com.github.laxika.magicalvibes.cards.n.NyxbornMarauder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoeStrider.class, NyxbornMarauder.class, HavengulLich.class})
class WoeStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 0/1 Goat token")
    void enteringCreatesGoatToken() {
        harness.setHand(player1, List.of(new WoeStrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent goat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(goat.getCard().isToken()).isTrue();
        assertThat(goat.getCard().getSubtypes()).containsExactly(CardSubtype.GOAT);
        assertThat(goat.getEffectivePower()).isZero();
        assertThat(goat.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing another creature allows Woe Strider to scry 1")
    void sacrificesAnotherCreatureAndScries() {
        Permanent woeStrider = addReadyWoeStrider();
        Permanent marauder = addCreatureReady(player1, new NyxbornMarauder());
        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(marauder.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(originalTop);
        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(
                permanent -> assertThat(permanent.getId()).isEqualTo(woeStrider.getId()));
    }

    @Test
    @DisplayName("Woe Strider cannot sacrifice itself for its ability")
    void cannotSacrificeItself() {
        addReadyWoeStrider();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Escaping exiles four other cards and adds two +1/+1 counters")
    void escapeExilesFourCardsAndAddsCounters() {
        WoeStrider woeStrider = new WoeStrider();
        NyxbornMarauder first = new NyxbornMarauder();
        NyxbornMarauder second = new NyxbornMarauder();
        NyxbornMarauder third = new NyxbornMarauder();
        NyxbornMarauder fourth = new NyxbornMarauder();
        harness.setGraveyard(player1, List.of(woeStrider, first, second, third, fourth));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);

        harness.passBothPriorities();

        Permanent escapedWoeStrider = findPermanent(player1, "Woe Strider");
        assertThat(escapedWoeStrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(escapedWoeStrider.getEffectivePower()).isEqualTo(5);
        assertThat(escapedWoeStrider.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Escape requires four other cards in the graveyard")
    void escapeRequiresFourOtherCards() {
        harness.setGraveyard(player1, List.of(new WoeStrider(), new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from hand does not add escape counters")
    void handCastDoesNotAddCounters() {
        harness.setHand(player1, List.of(new WoeStrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Woe Strider").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("An escaped Woe Strider still creates a white Goat")
    void escapedCreatureCreatesGoat() {
        harness.setGraveyard(player1, List.of(new WoeStrider(), new NyxbornMarauder(),
                new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        resolveAllTriggers();

        Permanent goat = findPermanent(player1, "Goat");
        assertThat(goat.getCard().isToken()).isTrue();
        assertThat(goat.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(goat.getEffectivePower()).isZero();
        assertThat(goat.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Woe Strider can sacrifice its Goat and put the top card on the bottom")
    void sacrificesGoatWhileSummoningSickAndScriesToBottom() {
        harness.setHand(player1, List.of(new WoeStrider()));
        harness.setLibrary(player1, List.of(new NyxbornMarauder(), new WoeStrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Card originalTop = gd.playerDecks.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(countPermanents(player1, "Goat")).isZero();
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(originalTop);
        harness.assertOnBattlefield(player1, "Woe Strider");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay Woe Strider's sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addReadyWoeStrider();
        addCreatureReady(player2, new NyxbornMarauder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Nyxborn Marauder");
    }

    @Test
    @DisplayName("Woe Strider cannot exile itself as one of the four escape cards")
    void cannotExileItselfForEscape() {
        harness.setGraveyard(player1, List.of(new WoeStrider(), new NyxbornMarauder(),
                new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Escape cannot exile the same graveyard card more than once")
    void cannotExileDuplicateCardsForEscape() {
        harness.setGraveyard(player1, List.of(new WoeStrider(), new NyxbornMarauder(),
                new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice can be paid even when the library is empty")
    void scryWithEmptyLibraryStillSacrificesCreature() {
        addReadyWoeStrider();
        addCreatureReady(player1, new NyxbornMarauder());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Marauder");
        harness.assertOnBattlefield(player1, "Woe Strider");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting with Havengul Lich's permission does not count as escaping")
    void graveyardCastWithoutEscapeDoesNotAddCounters() {
        addCreatureReady(player1, new HavengulLich());
        WoeStrider woeStrider = new WoeStrider();
        harness.setGraveyard(player1, List.of(woeStrider));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, woeStrider.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, woeStrider.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Woe Strider").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(countPermanents(player1, "Goat")).isEqualTo(1);
    }

    private Permanent addReadyWoeStrider() {
        return addCreatureReady(player1, new WoeStrider());
    }
}
