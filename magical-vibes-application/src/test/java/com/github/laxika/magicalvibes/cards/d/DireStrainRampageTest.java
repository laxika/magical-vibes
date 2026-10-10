package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrambleArmor;
import com.github.laxika.magicalvibes.cards.c.CurseOfShakenFaith;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireStrainRampage.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        Island.class, Mountain.class, BrambleArmor.class, CurseOfShakenFaith.class})
class DireStrainRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroying a land allows its controller to search for up to two basics")
    void landBranchSearchesForUpToTwoBasicLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Mountain(), new Island(), new GrizzlyBears()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Forest");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Mountain").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Island").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroying a nonland permanent allows its controller to search for one basic")
    void nonlandBranchSearchesForOneBasicLand() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Fountain of Youth"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(countPermanents(player2, "Mountain")).isEqualTo(1);
        assertThat(countPermanents(player2, "Island")).isZero();
    }

    @Test
    @DisplayName("An indestructible land uses the one-basic branch")
    void indestructibleLandUsesOneBasicBranch() {
        Forest forest = new Forest();
        forest.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        harness.addToBattlefield(player2, forest);
        harness.setLibrary(player2, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveRampageFromHand();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or land");
    }

    @Test
    @DisplayName("Flashback resolves and exiles Dire-Strain Rampage")
    void flashbackResolvesAndExiles() {
        DireStrainRampage spell = new DireStrainRampage();
        harness.setGraveyard(player1, List.of(spell));
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, harness.getPermanentId(player2, "Fountain of Youth"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("The target controller may decline the search without shuffling")
    void controllerMayDeclineSearch() {
        harness.addToBattlefield(player2, new Forest());
        List<com.github.laxika.magicalvibes.model.Card> library =
                List.of(new Mountain(), new Island(), new Forest());
        harness.setLibrary(player2, library);
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("The land controller can find only one of the two permitted basics")
    void landControllerCanFindOnlyOneBasic() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(findPermanent(player2, "Mountain").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targeting your own land lets you search your own library")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Forest"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An absent target causes the spell to do nothing")
    void missingTargetDoesNotSearch() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Dire-Strain Rampage");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("An enchantment is destroyed and its controller gets one tapped basic")
    void enchantmentControllerSearchesForOneBasic() {
        harness.addToBattlefield(player2, new CurseOfShakenFaith());
        findPermanent(player2, "Curse of Shaken Faith").setAttachedTo(player1.getId());
        harness.setLibrary(player2, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Curse of Shaken Faith"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Curse of Shaken Faith");
        assertThat(findPermanent(player2, "Mountain").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A MID artifact is a legal target and gives its controller one tapped basic")
    void artifactControllerSearchesForOneBasic() {
        harness.addToBattlefield(player2, new BrambleArmor());
        harness.setLibrary(player2, List.of(new Mountain(), new Island()));
        giveRampageFromHand();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Bramble Armor"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Bramble Armor");
        assertThat(findPermanent(player2, "Mountain").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void giveRampageFromHand() {
        harness.setHand(player1, List.of(new DireStrainRampage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

}
