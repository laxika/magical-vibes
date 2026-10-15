package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniCallerOfThePride;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraFlameshaper.class, AjaniCallerOfThePride.class, BurstLightning.class,
        Forest.class, LlanowarElves.class})
class ChandraFlameshaperTest extends BaseCardTest {

    @Test
    @DisplayName("+2 adds red mana and offers one of the top three cards to play this turn")
    void plusTwoAddsManaAndOffersOneCard() {
        addReadyChandra(player1, 4);
        Card chosen = new BurstLightning();
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
    @DisplayName("+1 creates a hasty creature-copy token whose sacrifice uses the stack")
    void plusOneCreatesHastyTokenCopy() {
        addReadyChandra(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Llanowar Elves");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).isNotEmpty();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("-4 divides damage between a creature and a planeswalker")
    void minusFourDamagesCreaturesAndPlaneswalkers() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniCallerOfThePride());
        ajani.setCounterCount(CounterType.LOYALTY, 5);
        ajani.setSummoningSick(false);

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
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraFlameshaper());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    @Test
    void plusTwoCanCastChosenSpellUsingGrantedMana() {
        addReadyChandra(player1, 4);
        Card chosen = new BurstLightning();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.castFromExile(player1, chosen.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(chosen);
    }

    @Test
    void plusTwoCanPlayChosenLand() {
        addReadyChandra(player1, 4);
        Card chosen = new Forest();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.castFromExile(player1, chosen.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(chosen);
    }

    @Test
    void plusTwoStillAddsManaWithEmptyLibrary() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void plusOneCannotCopyOpponentsCreature() {
        addReadyChandra(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneCannotCopyNoncreaturePermanent() {
        addReadyChandra(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusFourRejectsIncompleteDamageDivision() {
        addReadyChandra(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, null, Map.of(target.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusFourMayChooseNoTargets() {
        Permanent chandra = addReadyChandra(player1, 5);

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null, Map.of());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void minusFourDoesNotRedistributeDamageFromRemovedTarget() {
        addReadyChandra(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniCallerOfThePride());
        ajani.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null,
                Map.of(creature.getId(), 4, ajani.getId(), 4));
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }
}
