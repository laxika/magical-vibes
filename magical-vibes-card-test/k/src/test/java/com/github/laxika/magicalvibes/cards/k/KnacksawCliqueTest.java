package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MoonringIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnacksawClique.class, MoonringIsland.class})
class KnacksawCliqueTest extends BaseCardTest {

    @Test
    @DisplayName("Activating exiles the opponent's top card and lets the controller play it this turn")
    void exilesOpponentTopCardAndGrantsPlayPermission() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card top = new MoonringIsland();
        harness.setLibrary(player2, List.of(top));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        // Opponent's top card left their library and the controller may play it.
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());

        // The controller can actually play the exiled land onto their own battlefield.
        harness.castFromExile(player1, top.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("Untaps the source when paying {Q}")
    void payingUntapCostUntapsSource() {
        Permanent clique = addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new MoonringIsland()));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(clique.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new MoonringIsland()));

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Requires both the generic and blue mana in its activation cost")
    void requiresGenericAndBlueMana() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player2, List.of(new MoonringIsland()));

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Only an opponent can be chosen as the target")
    void cannotTargetController() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Play permission expires at the end of the turn")
    void playPermissionExpiresAtEndOfTurn() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        MoonringIsland top = new MoonringIsland();
        harness.setLibrary(player2, List.of(top));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }

    @Test
    @DisplayName("Does nothing when the targeted opponent's library is empty")
    void doesNothingWhenOpponentLibraryIsEmpty() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of());

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick creature cannot pay the untap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent clique = addTapped(player1, new KnacksawClique());
        clique.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(clique.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An exiled spell still requires its normal mana cost")
    void castsExiledSpellOnlyAfterPayingNormalCost() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        KnacksawClique top = new KnacksawClique();
        harness.setLibrary(player2, List.of(top));
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("Play permission does not let a land be played on the opponent's turn")
    void cannotPlayExiledLandOnOpponentsTurn() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        MoonringIsland top = new MoonringIsland();
        harness.setLibrary(player2, List.of(top));
        enterMainWithPriority(player2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot play a land");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
    }

    @Test
    @DisplayName("An exiled creature must still be cast at sorcery speed")
    void cannotCastExiledCreatureOutsideMainPhase() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 6);
        KnacksawClique top = new KnacksawClique();
        harness.setLibrary(player2, List.of(top));
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
    }

    @Test
    @DisplayName("Play permission does not grant an additional land play")
    void cannotPlayExiledLandAfterUsingLandPlay() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player1, List.of(new MoonringIsland()));
        MoonringIsland top = new MoonringIsland();
        harness.setLibrary(player2, List.of(top));
        enterMainWithPriority(player1);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot play a land");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
    }

    @Test
    @DisplayName("The ability and play permission survive the source leaving the battlefield")
    void resolvesAndAllowsPlayWithoutSource() {
        addTapped(player1, new KnacksawClique());
        harness.addMana(player1, ManaColor.BLUE, 2);
        MoonringIsland top = new MoonringIsland();
        harness.setLibrary(player2, List.of(top));
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.castFromExile(player1, top.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(top);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
