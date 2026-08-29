package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentalOverloadTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a blue and red Weird sized by instant and sorcery cards in the graveyard")
    void createsWeirdSizedByInstantAndSorceryCards() {
        harness.setHand(player1, List.of(new ExperimentalOverload()));
        harness.setGraveyard(player1, List.of(new Opt(), new ThinkTwice(), new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        Permanent weird = findWeird();
        assertThat(gqs.getEffectivePower(gd, weird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, weird)).isEqualTo(2);
        assertThat(weird.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Experimental Overload"));
    }

    @Test
    @DisplayName("May return a chosen instant or sorcery and then exiles itself")
    void mayReturnChosenInstantOrSorcery() {
        harness.setHand(player1, List.of(new ExperimentalOverload()));
        harness.setGraveyard(player1, List.of(new Opt(), new ThinkTwice(), new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Think Twice");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Experimental Overload"));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent findWeird() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Weird"))
                .findFirst()
                .orElseThrow();
    }
}
