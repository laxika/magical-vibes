package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OliviasBloodsworn.class, QuilledWolf.class})
class OliviasBloodswornTest extends BaseCardTest {

    @Test
    @DisplayName("Olivia's Bloodsworn cannot block")
    void cannotBlock() {
        Permanent bloodsworn = harness.addToBattlefieldAndReturn(player2, new OliviasBloodsworn());
        bloodsworn.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("The red ability gives a target Vampire haste until end of turn")
    void grantsHasteToTargetVampire() {
        harness.addToBattlefield(player1, new OliviasBloodsworn());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(wolf.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The red ability cannot target a non-Vampire")
    void cannotTargetNonVampire() {
        harness.addToBattlefield(player1, new OliviasBloodsworn());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A newly entered Bloodsworn can give itself haste and attack")
    void canGrantItselfHasteAndAttack() {
        Permanent bloodsworn = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        bloodsworn.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, bloodsworn.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bloodsworn.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opponent's Vampire without granting haste to its source")
    void canTargetOpponentsVampire() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OliviasBloodsworn());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(source.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A tapped Bloodsworn can activate its ability")
    void canActivateWhileTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        source.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires red mana")
    void cannotPayWithBlackMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A Vampire replacing the departed target does not gain haste")
    void doesNotGrantHasteToReplacementPermanent() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        harness.passBothPriorities();

        assertThat(replacement.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(source.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attacking Bloodsworn cannot be blocked by a creature without flying or reach")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        blocker.setSummoningSick(false);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
