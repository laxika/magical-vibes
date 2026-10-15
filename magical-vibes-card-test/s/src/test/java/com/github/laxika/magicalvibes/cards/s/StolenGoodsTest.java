package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StolenGoods.class, Forest.class, GrizzlyBears.class, Vorstclaw.class, BoneSplinters.class})
class StolenGoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles through lands until a nonland card and grants a free cast this turn")
    void exilesUntilNonlandAndGrantsFreeCast() {
        Forest land1 = new Forest();
        Forest land2 = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land1, land2, bears));

        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest", "Grizzly Bears");
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(bears.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land2.getId());
    }

    @Test
    @DisplayName("Free-cast permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(bears.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castsStolenCreatureForFreeAndStopsAtFirstNonland() {
        Vorstclaw stolen = new Vorstclaw();
        Forest remaining = new Forest();
        harness.setLibrary(player2, List.of(new Forest(), stolen, remaining));
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        gd.playerManaPools.get(player1.getId()).clear();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        harness.castFromExile(player1, stolen.getId());
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getOwnerIdOverride()).isEqualTo(player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vorstclaw");
        harness.assertNotOnBattlefield(player2, "Vorstclaw");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(stolen);
    }

    @Test
    void allLandLibraryIsExiledWithoutGrantingPermission() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
    }

    @Test
    void emptyLibraryDoesNotGrantPermission() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        harness.assertInGraveyard(player1, "Stolen Goods");
    }

    @Test
    void stolenCreatureStillRequiresNormalCastingTiming() {
        Vorstclaw stolen = new Vorstclaw();
        harness.setLibrary(player2, List.of(stolen));
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(stolen);
    }

    @Test
    void opponentCannotUseGrantedCastingPermission() {
        Vorstclaw stolen = new Vorstclaw();
        harness.setLibrary(player2, List.of(stolen));
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.addMana(player2, ManaColor.GREEN, 6);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromExile(player2, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(stolen);
    }

    @Test
    void canBeginCastingStolenSpellWithPayableSacrificeCost() {
        BoneSplinters stolen = new BoneSplinters();
        harness.addToBattlefield(player1, new Vorstclaw());
        var target = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.setLibrary(player2, List.of(stolen));
        harness.setHand(player1, List.of(new StolenGoods()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatCode(() -> harness.castFromExile(player1, stolen.getId(), target.getId()))
                .doesNotThrowAnyException();
    }
}
