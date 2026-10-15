package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgonyWarp;
import com.github.laxika.magicalvibes.cards.k.KederektCreeper;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseConnoisseur.class, DregscapeZombie.class, KederektCreeper.class, AgonyWarp.class})
@DisplayName("Corpse Connoisseur")
class CorpseConnoisseurTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search puts a chosen creature card into the graveyard")
    void etbSearchPutsCreatureIntoGraveyard() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new DregscapeZombie(), new KederektCreeper()));

        harness.passBothPriorities(); // Resolve the creature and trigger its ETB ability
        harness.passBothPriorities(); // Resolve the optional search trigger
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).get(0).getName())
                .isEqualTo("Dregscape Zombie");
    }

    @Test
    @DisplayName("Declining the may ability does not search the library")
    void decliningSkipsSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new DregscapeZombie()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unearth returns Corpse Connoisseur with haste and exiles it at end step")
    void unearthReturnsWithHasteThenExiles() {
        CorpseConnoisseur card = new CorpseConnoisseur();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // Resolve unearth and trigger the ETB ability
        harness.passBothPriorities(); // Resolve the optional search trigger
        harness.handleMayAbilityChosen(player1, false); // Decline the search

        Permanent perm = findPermanent(player1, "Corpse Connoisseur");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Corpse Connoisseur");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Corpse Connoisseur"));
    }

    @Test
    void searchCanFailToFindEvenWithCreatureAvailable() {
        setupAndCast();
        CorpseConnoisseur creature = new CorpseConnoisseur();
        AgonyWarp instant = new AgonyWarp();
        harness.setLibrary(player1, List.of(creature, instant));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, instant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchWithOnlyNoncreaturesFindsNothing() {
        setupAndCast();
        AgonyWarp instant = new AgonyWarp();
        harness.setLibrary(player1, List.of(instant));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthTriggersSearchAndPutsCreatureInGraveyard() {
        CorpseConnoisseur card = new CorpseConnoisseur();
        CorpseConnoisseur searchedCard = new CorpseConnoisseur();
        harness.setGraveyard(player1, List.of(card));
        harness.setLibrary(player1, List.of(searchedCard, new AgonyWarp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Corpse Connoisseur");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(searchedCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void unearthedCreatureIsExiledInsteadOfDying() {
        CorpseConnoisseur card = new CorpseConnoisseur();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent permanent = findPermanent(player1, "Corpse Connoisseur");
        harness.setHand(player2, List.of(new AgonyWarp()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, List.of(permanent.getId(), permanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Corpse Connoisseur");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void unearthCannotBeActivatedOutsideMainPhase() {
        CorpseConnoisseur card = new CorpseConnoisseur();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedWithSpellOnStack() {
        CorpseConnoisseur card = new CorpseConnoisseur();
        harness.setGraveyard(player1, List.of(card));
        setupAndCast();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).hasSize(1);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new CorpseConnoisseur()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
