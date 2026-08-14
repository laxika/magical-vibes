package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandraFlameshaperTest extends BaseCardTest {

    @Test
    @DisplayName("+2 adds red mana and offers one of the top three cards to play this turn")
    void plusTwoAddsManaAndOffersOneCard() {
        addReadyChandra(player1, 4);
        Card chosen = new Shock();
        Card other = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(chosen, other, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, other, third);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exilePlayPermissions.get(chosen.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(chosen.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(other.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(third.getId());
    }

    @Test
    @DisplayName("+1 creates a hasty creature-copy token scheduled for sacrifice")
    void plusOneCreatesHastyTokenCopy() {
        addReadyChandra(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(),
                        DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("-4 divides damage between a creature and a planeswalker")
    void minusFourDamagesCreaturesAndPlaneswalkers() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ajani = new Permanent(new AjaniOutlandChaperone());
        ajani.setCounterCount(CounterType.LOYALTY, 5);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ajani);

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null,
                Map.of(bear.getId(), 4, ajani.getId(), 4));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bear.getId()));
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-4 cannot target a player")
    void minusFourCannotTargetPlayer() {
        addReadyChandra(player1, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, null, Map.of(player2.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = new Permanent(new ChandraFlameshaper());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
