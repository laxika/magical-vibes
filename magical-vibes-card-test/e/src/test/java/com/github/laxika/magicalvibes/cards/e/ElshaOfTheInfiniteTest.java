package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElshaOfTheInfinite.class, Divination.class, Forest.class, GrizzlyBears.class})
class ElshaOfTheInfiniteTest extends BaseCardTest {

    @Test
    void castsNoncreatureSpellFromLibraryTopWithFlashAndTriggersProwess() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(divination, new Forest(), new Forest()));
        int startingHandSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 2);
    }

    @Test
    void topLibraryPermissionDoesNotGiveFlashToHandSpells() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureOnLibraryTopCannotBeCast() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void prowessTriggersOnlyOnceForANoncreatureSpellFromHand() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(3);
    }

    @Test
    void topCardIsVisibleOnlyToElshasControllerEvenWhenItIsALand() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void elshaOnTopOfLibraryDoesNotGrantPermissionToLookAtIt() {
        ElshaOfTheInfinite top = new ElshaOfTheInfinite();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn1().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void cannotPlayLandFromLibraryTop() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void stillRequiresCorrectColoredManaForLibrarySpell() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        Divination top = new Divination();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void canCastSuccessiveTopCardsWithASpellStillOnTheStack() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        Divination first = new Divination();
        Divination second = new Divination();
        harness.setLibrary(player1, List.of(first, second, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(5);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        harness.setHand(player2, List.of(new Divination()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(3);
    }

    @Test
    void losingAbilitiesRemovesLibraryCastingAndViewingPermissions() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        elsha.setLosesAllAbilitiesUntilEndOfTurn(true);
        Divination top = new Divination();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn1().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }
}
