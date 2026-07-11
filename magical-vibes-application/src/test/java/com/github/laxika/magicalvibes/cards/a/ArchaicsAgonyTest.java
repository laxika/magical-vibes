package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchaicsAgonyTest extends BaseCardTest {

    

    @Test
    @DisplayName("Converge X equals number of colors spent to cast")
    void convergeXEqualsColorsSpent() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, target.getId());

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Colorless mana alone gives converge X of 1 when only one colored mana is spent")
    void singleColorGivesConvergeOne() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals converge damage to target creature")
    void dealsConvergeDamage() {
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles top cards equal to excess damage and grants play permission")
    void exilesTopCardsForExcessDamage() {
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        Card topCard = new Shock();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("No excess damage means no library cards exiled")
    void noExcessDamageExilesNothing() {
        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        Card topCard = new Shock();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Exile play permission expires at end of controller's next turn")
    void playPermissionExpiresAtCorrectTurn() {
        Permanent target = new Permanent(new RagingGoblin()); // 1/1
        gd.playerBattlefields.get(player2.getId()).add(target);

        Card topCard = new Shock();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        UUID exiledId = topCard.getId();
        int expireTurn = gd.exilePlayPermissionsExpireAtTurnEnd.get(exiledId);
        assertThat(expireTurn).isGreaterThan(gd.turnNumber);

        gd.turnNumber = expireTurn - 1;
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        assertThat(gd.exilePlayPermissions).containsKey(exiledId);

        gd.turnNumber = expireTurn;
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiledId);
    }

    @Test
    @DisplayName("Rejects illegal non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new ArchaicsAgony()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
