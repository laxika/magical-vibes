package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClanGuildmage.class, Forest.class})
class ClanGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("The red ability prevents the target creature from blocking this turn")
    void redAbilityPreventsBlocking() {
        addCreatureReady(player1, new ClanGuildmage());
        Permanent attacker = addCreatureReady(player1, new ClanGuildmage());
        Permanent target = addCreatureReady(player2, new ClanGuildmage());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isCantBlockThisTurn()).isTrue();
        target.resetModifiers();
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The green ability animates a controlled land until end of turn")
    void greenAbilityAnimatesControlledLand() {
        addCreatureReady(player1, new ClanGuildmage());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();

        land.resetModifiers();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    @DisplayName("The green ability cannot target an opponent's land")
    void greenAbilityRequiresControlledLand() {
        addCreatureReady(player1, new ClanGuildmage());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redAbilityCanTargetItsOwnSourceAndPaysTapCostImmediately() {
        Permanent guildmage = addCreatureReady(player1, new ClanGuildmage());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, guildmage.getId());

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(guildmage.isCantBlockThisTurn()).isFalse();
        harness.passBothPriorities();
        assertThat(guildmage.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void redAbilityCannotTargetANoncreatureLand() {
        addCreatureReady(player1, new ClanGuildmage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void greenAbilityCannotTargetANonlandCreature() {
        Permanent guildmage = addCreatureReady(player1, new ClanGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animatedLandRetainsItsLandSubtypeAndCanAttackImmediately() {
        Permanent guildmage = addCreatureReady(player1, new ClanGuildmage());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, land.getId());
        assertThat(guildmage.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.FOREST)).isTrue();
        declareAttackersAndPrepareBlockers(List.of(1));
        assertThat(land.isAttacking()).isTrue();
    }
}
