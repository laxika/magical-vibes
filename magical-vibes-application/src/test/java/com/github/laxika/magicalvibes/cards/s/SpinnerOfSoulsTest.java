package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpinnerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("A dying nontoken creature triggers Spinner and finds a creature card")
    void dyingNontokenCreatureFindsCreatureCard() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Forest forest = new Forest();
        Island island = new Island();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island, creature));
        killCreatureWithShock(bearsId);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
    }

    @Test
    @DisplayName("Declining Spinner of Souls leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Forest forest = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, creature));
        killCreatureWithShock(bearsId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, creature);
    }

    @Test
    @DisplayName("A token creature dying does not trigger Spinner of Souls")
    void tokenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpinnerOfSouls());
        Permanent token = addTokenCreature(player1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        killCreatureWithShock(token.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void killCreatureWithShock(UUID creatureId) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creatureId);
        harness.passBothPriorities();
    }

    private Permanent addTokenCreature(Player owner) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(owner.getId()).add(token);
        return token;
    }
}
