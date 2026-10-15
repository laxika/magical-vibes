package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncineratingBlast.class, GrizzlyBears.class, LlanowarElves.class, FountainOfYouth.class})
class IncineratingBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to a target creature and allows declining the discard")
    void dealsDamageAndMayDeclineDiscard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncineratingBlast()));
        addMana();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Incinerating Blast");
    }

    @Test
    @DisplayName("Accepting the discard draws a card after discarding")
    void acceptingDiscardDrawsCard() {
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new IncineratingBlast(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new IncineratingBlast()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Fountain of Youth")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty hand cannot produce a draw from the optional discard")
    void emptyHandDoesNotDraw() {
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new IncineratingBlast()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Incinerating Blast");
    }

    @Test
    @DisplayName("An illegal sole target prevents the optional discard and draw")
    void illegalTargetPreventsDiscardAndDraw() {
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new IncineratingBlast(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Incinerating Blast");
    }

    @Test
    @DisplayName("Deals exactly six damage and can target the controller's creature")
    void dealsExactlySixDamageToOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var target = gd.playerBattlefields.get(player1.getId()).getFirst();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new IncineratingBlast()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Incinerating Blast");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

}
