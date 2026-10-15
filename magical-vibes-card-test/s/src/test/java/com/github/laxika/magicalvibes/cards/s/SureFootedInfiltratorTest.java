package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MerfolkWindrobber;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SureFootedInfiltrator.class, MerfolkWindrobber.class, Forest.class})
class SureFootedInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Rogue makes Sure-Footed Infiltrator unblockable this turn")
    void tappingAnotherRogueMakesItUnblockable() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());
        Permanent rogue = addCreatureReady(player1, new MerfolkWindrobber());

        harness.activateAbility(player1, battlefieldIndex(infiltrator), 0, null, null);
        harness.passBothPriorities();

        assertThat(rogue.isTapped()).isTrue();
        assertThat(infiltrator.isTapped()).isFalse();
        assertThat(infiltrator.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Sure-Footed Infiltrator cannot tap itself for its ability")
    void cannotTapItself() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(infiltrator), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat damage to a player draws a card")
    void drawsOnCombatDamageToPlayer() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());
        infiltrator.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void canTapSummoningSickRogueWhileSourceIsTapped() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SureFootedInfiltrator());
        infiltrator.tap();
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new MerfolkWindrobber());
        rogue.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(infiltrator), 0, null, null);
        harness.passBothPriorities();

        assertThat(rogue.isTapped()).isTrue();
        assertThat(infiltrator.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotTapAlreadyTappedRogue() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());
        Permanent rogue = addCreatureReady(player1, new MerfolkWindrobber());
        rogue.tap();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(infiltrator), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(infiltrator.isCantBeBlocked()).isFalse();
    }

    @Test
    void cannotTapOpponentsRogue() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());
        Permanent rogue = addCreatureReady(player2, new MerfolkWindrobber());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(infiltrator), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rogue.isTapped()).isFalse();
    }

    @Test
    void cannotTapNonRoguePermanent() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(infiltrator), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void combatDamageToCreatureDoesNotDraw() {
        addCreatureReady(player1, new SureFootedInfiltrator());
        addCreatureReady(player2, new SureFootedInfiltrator());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void activatedAbilityPreventsBlockDeclaration() {
        Permanent infiltrator = addCreatureReady(player1, new SureFootedInfiltrator());
        addCreatureReady(player1, new MerfolkWindrobber());
        addCreatureReady(player2, new SureFootedInfiltrator());

        harness.activateAbility(player1, battlefieldIndex(infiltrator), 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
