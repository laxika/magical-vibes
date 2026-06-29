package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhostQuarterTest extends BaseCardTest {

    @Test
    @DisplayName("Ghost Quarter has correct abilities")
    void hasCorrectAbilities() {
        GhostQuarter card = new GhostQuarter();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {T}: Add {C}
        var manaAbility = card.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        // Second ability: {T}, Sacrifice: Destroy target land...
        var destroyAbility = card.getActivatedAbilities().get(1);
        assertThat(destroyAbility.isRequiresTap()).isTrue();
        assertThat(destroyAbility.getManaCost()).isNull();
        assertThat(destroyAbility.getEffects()).hasSize(2);
        assertThat(destroyAbility.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(destroyAbility.getEffects().get(1))
                .isInstanceOf(DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect.class);
    }

    @Test
    @DisplayName("Activating destroy ability sacrifices Ghost Quarter and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ghost Quarter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ghost Quarter"));
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving destroys target land and presents basic land search to its controller")
    void destroysLandAndPresentsSearch() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Target land is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));

        // Player 2 (the land's controller) is prompted to search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Land's controller can choose a basic land to put onto the battlefield untapped")
    void controllerChoosesBasicLandUntapped() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
        // The chosen land enters untapped
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && !p.isTapped());
    }

    @Test
    @DisplayName("Land's controller can fail to find (may search)")
    void controllerCanFailToFind() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player2, -1);

        // No new land on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains")
                        || p.getCard().getName().equals("Island")
                        || p.getCard().getName().equals("Mountain"));
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Can target own land")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player1, new Forest());
        UUID targetId = harness.getPermanentId(player1, "Forest");
        setupLibrary(player1);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Own land is destroyed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));

        // Player 1 is prompted to search their own library
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("No search prompt when land controller's library has no basic lands")
    void noBasicLandsNoPrompt() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        // Set up library with no basic lands
        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can tap for colorless mana with first ability")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new GhostQuarter());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Island(), new Mountain(), new GrizzlyBears()));
    }
}
