package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.t.Trickbind;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggWarMarshal.class, WrathOfGod.class, Trickbind.class})
class MoggWarMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Goblin, and declining echo creates another when it dies")
    void entersAndDiesCreateGoblins() {
        castAndResolveMoggWarMarshal();

        assertThat(goblinTokens()).hasSize(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mogg War Marshal");
        assertThat(goblinTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Entering creates a 1/1 red Goblin creature token")
    void enteringCreatesCorrectGoblinToken() {
        castAndResolveMoggWarMarshal();

        Permanent token = goblinTokens().getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Paying echo keeps Mogg War Marshal and echo does not trigger again")
    void payingEchoKeepsMarshalAndIsOneShot() {
        castAndResolveMoggWarMarshal();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Mogg War Marshal");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mogg War Marshal");
    }

    @Test
    @DisplayName("Echo waits for Mogg War Marshal's controller's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveMoggWarMarshal();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mogg War Marshal");
    }

    @Test
    @DisplayName("A Mogg War Marshal death trigger creates a Goblin")
    void deathCreatesGoblin() {
        harness.addToBattlefield(player1, new MoggWarMarshal());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(goblinTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Countering the enters trigger does not remove echo")
    void counteringEntersTriggerDoesNotRemoveEcho() {
        harness.castFromHand(player1, new MoggWarMarshal(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Trickbind()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gd.stack.getLast().getCard().getId());
        resolveAllTriggers();

        assertThat(goblinTokens()).isEmpty();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mogg War Marshal");
        assertThat(goblinTokens()).hasSize(1);
    }

    @Test
    @DisplayName("A simultaneous death of the Marshal and its Goblin still creates a new Goblin")
    void simultaneousDeathsCreateNewGoblin() {
        castAndResolveMoggWarMarshal();
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mogg War Marshal");
        assertThat(goblinTokens()).hasSize(1);
    }

    private void castAndResolveMoggWarMarshal() {
        harness.castFromHand(player1, new MoggWarMarshal(), "{1}{R}");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Mogg War Marshal");
    }

    private List<Permanent> goblinTokens() {
        return findPermanents(player1, "Goblin").stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }
}
