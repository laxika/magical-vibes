package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZukoExiledPrince.class, Forest.class})
class ZukoExiledPrinceTest extends BaseCardTest {

    @Test
    void attackingAddsThreeRedManaUntilEndOfCombat() {
        addReadyZuko();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void abilityExilesTopCardWithPlayPermissionUntilEndOfTurn() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addReadyZuko();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void firebendingManaPaysForAbilityWhileZukoIsAttacking() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addReadyZuko();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void exiledLandCanBePlayedAfterZukoLeavesBattlefield() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addReadyZuko();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void activatingWithEmptyLibraryDoesNotDrawOrLoseGame() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new ZukoExiledPrince());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        ZukoExiledPrince topCard = new ZukoExiledPrince();
        harness.setLibrary(player1, List.of(topCard));
        addReadyZuko();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.RED, 4);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addReadyZuko() {
        return addCreatureReady(player1, new ZukoExiledPrince());
    }
}
