package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PartInFriendship.class, Forest.class, GrizzlyBears.class, Shock.class})
class PartInFriendshipTest extends BaseCardTest {

    @Test
    void putsRevealedCreatureOntoBattlefieldWhenItsManaValueIsWithinLandCount() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card revealedBeforeCreature = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealedBeforeCreature, creature));

        killCreature(player2, player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealedBeforeCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void putsRevealedCreatureIntoHandWhenItsManaValueExceedsLandCount() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        killCreature(player2, player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCreature, secondCreature));

        killCreature(player2, player1);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature);

        UUID remainingCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        killCreatureById(player2, remainingCreatureId);

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstCreature)
                .doesNotContain(secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).contains(secondCreature);
    }

    @Test
    void ignoresOpponentCreatureDeaths() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        killCreature(player1, player2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void tokenDeathDoesNotTriggerOrUseTheTurnLimit() {
        harness.addToBattlefield(player1, new PartInFriendship());
        Card token = new GrizzlyBears();
        token.setToken(true);
        UUID tokenId = harness.addToBattlefieldAndReturn(player1, token).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        killCreatureById(player2, tokenId);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);

        killCreature(player2, player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsEveryRevealedCardWhenLibraryContainsNoCreatures() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card land = new Forest();
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(land, spell));

        killCreature(player2, player1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, spell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land, spell);
    }

    @Test
    void leavesUnrevealedCardsAboveTheRandomlyOrderedRest() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card firstRevealed = new Forest();
        Card secondRevealed = new Shock();
        Card creature = new GrizzlyBears();
        Card unrevealed = new Forest();
        harness.setLibrary(player1, List.of(firstRevealed, secondRevealed, creature, unrevealed));

        killCreature(player2, player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(firstRevealed, secondRevealed);
    }

    @Test
    void countsLandsAtResolutionRatherThanWhenCreatureDies() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void canTriggerAgainDuringTheNextPlayersTurn() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCreature, secondCreature));

        killCreature(player2, player1);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(firstCreature, new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesHarmlesslyWithAnEmptyLibrary() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        killCreature(player2, player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void eachEnchantmentCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new PartInFriendship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCreature, secondCreature));

        killCreature(player2, player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void killCreature(com.github.laxika.magicalvibes.model.Player caster,
            com.github.laxika.magicalvibes.model.Player targetController) {
        UUID targetId = harness.getPermanentId(targetController, "Grizzly Bears");
        killCreatureById(caster, targetId);
    }

    private void killCreatureById(com.github.laxika.magicalvibes.model.Player caster,
            UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
