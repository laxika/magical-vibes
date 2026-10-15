package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Transmogrify.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class,
        SoulWarden.class, GrafdiggersCage.class})
class TransmogrifyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target creature and puts the first revealed creature onto its controller's battlefield")
    void exilesTargetAndReplacesItWithRevealedCreature() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        harness.setLibrary(player2, List.of(new FountainOfYouth(), new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Shuffles the revealed cards back when no creature is found")
    void shufflesRevealedCardsBackWhenNoCreatureIsFound() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        harness.setLibrary(player1, List.of(new FountainOfYouth()));

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
        assertThat(gameData.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stopsAtFirstCreatureAndPreservesEveryOtherLibraryCard() {
        harness.addToBattlefield(player2, new LlanowarElves());
        FountainOfYouth revealed = new FountainOfYouth();
        GrizzlyBears firstCreature = new GrizzlyBears();
        LlanowarElves laterCreature = new LlanowarElves();
        FountainOfYouth unrevealed = new FountainOfYouth();
        harness.setLibrary(player2, List.of(revealed, firstCreature, laterCreature, unrevealed));
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(firstCreature.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(revealed.getId(), laterCreature.getId(), unrevealed.getId());
    }

    @Test
    void usesControllersLibraryButExilesToOwnersZone() {
        LlanowarElves stolenCreature = new LlanowarElves();
        stolenCreature.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, stolenCreature);
        GrizzlyBears replacement = new GrizzlyBears();
        FountainOfYouth ownersLibraryCard = new FountainOfYouth();
        harness.setLibrary(player2, List.of(replacement));
        harness.setLibrary(player1, List.of(ownersLibraryCard));
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(stolenCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(stolenCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownersLibraryCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void stillExilesCreatureWhenLibraryIsEmpty() {
        LlanowarElves target = new LlanowarElves();
        harness.addToBattlefield(player2, target);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void illegalTargetDoesNotRevealOrReplaceAnything() {
        var target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Transmogrify");
    }

    @Test
    void revealedCreatureTriggersBattlefieldEntryAbilities() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
    }

    @Test
    void exilingCreatureTokenStillRevealsReplacement() {
        LlanowarElves tokenCopy = new LlanowarElves();
        tokenCopy.setToken(true);
        harness.addToBattlefield(player2, tokenCopy);
        GrizzlyBears replacement = new GrizzlyBears();
        harness.setLibrary(player2, List.of(replacement));
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void cagePreventsRevealedCreatureFromEnteringAndKeepsItInLibrary() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.addToBattlefield(player2, new LlanowarElves());
        FountainOfYouth noncreature = new FountainOfYouth();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(noncreature, creature));
        harness.setHand(player1, List.of(new Transmogrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(noncreature, creature);
    }
}
