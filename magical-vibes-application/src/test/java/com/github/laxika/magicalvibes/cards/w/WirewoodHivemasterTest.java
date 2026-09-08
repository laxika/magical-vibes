package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WirewoodHivemaster.class, LlanowarElves.class, GrizzlyBears.class})
class WirewoodHivemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken Elf entering may create a 1/1 green Insect token")
    void elfEntryMayCreateInsect() {
        harness.addToBattlefield(player1, new WirewoodHivemaster());
        castCreature(player1, new LlanowarElves(), ManaColor.GREEN, 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Insect")).hasSize(1)
                .allMatch(permanent -> permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getColors().contains(CardColor.GREEN)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.INSECT));
    }

    @Test
    @DisplayName("Declining the trigger creates no Insect token")
    void decliningCreatesNoToken() {
        harness.addToBattlefield(player1, new WirewoodHivemaster());
        castCreature(player1, new LlanowarElves(), ManaColor.GREEN, 1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    @DisplayName("A non-Elf creature does not trigger Wirewood Hivemaster")
    void nonElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new WirewoodHivemaster());
        castCreature(player1, new GrizzlyBears(), ManaColor.GREEN, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's nontoken Elf entering may create an Insect")
    void opponentElfTriggers() {
        harness.addToBattlefield(player1, new WirewoodHivemaster());
        castCreature(player2, new LlanowarElves(), ManaColor.GREEN, 1);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Insect")).hasSize(1);
    }

    @Test
    void tokenElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new WirewoodHivemaster());
        Card elf = new LlanowarElves();
        elf.setToken(true);

        harness.enterBattlefieldAndReturn(player2, elf);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    void itsOwnEntryDoesNotTrigger() {
        castCreature(player1, new WirewoodHivemaster(), ManaColor.GREEN, 2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    private void castCreature(Player player,
                              Card creature,
                              ManaColor color,
                              int amount) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(creature));
        harness.addMana(player, color, amount);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
