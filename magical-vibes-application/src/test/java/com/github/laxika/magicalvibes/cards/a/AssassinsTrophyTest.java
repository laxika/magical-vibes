package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImperviousGreatwurm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssassinsTrophy.class, Forest.class, Plains.class, ImperviousGreatwurm.class, WateryGrave.class})
class AssassinsTrophyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target permanent and prompts its controller to search for a basic land")
    void destroysPermanentAndPresentsSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        setupLibrary(player2);
        castTrophy(target);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Its controller can put the chosen basic land onto the battlefield untapped")
    void chosenLandEntersUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        setupLibrary(player2);
        castTrophy(target);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        Set<UUID> battlefieldBefore = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .collect(Collectors.toSet());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> !battlefieldBefore.contains(p.getId()) && !p.isTapped());
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AssassinsTrophy()));
        addTrophyMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Declining the optional search does not shuffle the library")
    void decliningSearchDoesNotShuffle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        setupLibrary(player2);
        List<Card> libraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        castTrophy(target);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(libraryBefore);
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().toLowerCase().contains("shuffl"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An indestructible target survives and its controller can still find a land")
    void indestructibleTargetStillAllowsSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ImperviousGreatwurm());
        Plains land = new Plains();
        harness.setLibrary(player2, List.of(land));
        castTrophy(target);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getCard() == land && !p.isTapped());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("A target that leaves the battlefield before resolution grants no search")
    void missingTargetDoesNotSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        setupLibrary(player2);
        castTrophy(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().toLowerCase().contains("search"));
    }

    @Test
    @DisplayName("A nonbasic land with basic land types cannot be found")
    void searchExcludesNonbasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        WateryGrave nonbasic = new WateryGrave();
        Plains basic = new Plains();
        harness.setLibrary(player2, List.of(nonbasic, basic));
        castTrophy(target);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getCard() == basic);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonbasic);
    }

    private void castTrophy(Permanent target) {
        harness.setHand(player1, List.of(new AssassinsTrophy()));
        addTrophyMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addTrophyMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Forest()));
    }
}
