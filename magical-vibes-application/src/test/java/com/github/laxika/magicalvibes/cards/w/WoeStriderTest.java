package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoeStrider.class, GrizzlyBears.class})
class WoeStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 0/1 Goat token")
    void enteringCreatesGoatToken() {
        harness.setHand(player1, List.of(new WoeStrider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bear.getId()));
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
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
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
        harness.setGraveyard(player1, List.of(new WoeStrider(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWoeStrider() {
        return addCreatureReady(player1, new WoeStrider());
    }
}
