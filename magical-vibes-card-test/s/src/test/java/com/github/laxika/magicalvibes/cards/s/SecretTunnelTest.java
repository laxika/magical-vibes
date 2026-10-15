package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecretTunnel.class, GrizzlyBears.class, LlanowarElves.class})
class SecretTunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Secret Tunnel adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent tunnel = harness.addToBattlefieldAndReturn(player1, new SecretTunnel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(tunnel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two controlled creatures sharing a type can't be blocked this turn")
    void makesTwoCreaturesUnblockable() {
        harness.addToBattlefieldAndReturn(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target two controlled creatures that share no creature type")
    void rejectsCreaturesWithoutSharedType() {
        harness.addToBattlefieldAndReturn(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a creature type");
    }

    @Test
    @DisplayName("The Secret Tunnel ability only targets creatures you control")
    void rejectsOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    void animatedTunnelCannotBeBlocked() {
        Permanent tunnel = harness.addToBattlefieldAndReturn(player1, new SecretTunnel());
        tunnel.setAnimatedUntilEndOfTurn(true);
        tunnel.setAnimatedPower(2);
        tunnel.setAnimatedToughness(2);

        assertThat(gqs.isCreature(gd, tunnel)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, tunnel)).isTrue();
    }

    @Test
    void requiresTwoDistinctTargets() {
        harness.addToBattlefield(player1, new SecretTunnel());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isCantBeBlocked()).isFalse();
    }

    @Test
    void unblockabilityExpiresAtEndOfTurn() {
        Permanent tunnel = harness.addToBattlefieldAndReturn(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        assertThat(tunnel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, first)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, second)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, first)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, second)).isFalse();
    }

    @Test
    void remainingTargetStillBecomesUnblockableWhenOtherTargetLeaves() {
        harness.addToBattlefield(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, first)).isTrue();
    }

    @Test
    void doesNotResolveWhenTargetsNoLongerShareCreatureType() {
        harness.addToBattlefield(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        second.setTransientCreatureTypeOverride(CardSubtype.FROG);
        assertThat(gqs.shareCreatureType(gd, first, second)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, first)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, second)).isFalse();
    }
    @Test
    void remainingTargetMustStillShareDepartedTargetsLastKnownType() {
        harness.addToBattlefield(player1, new SecretTunnel());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        second.setLastKnownSubtypes(java.util.Set.of(CardSubtype.BEAR));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        first.setTransientCreatureTypeOverride(CardSubtype.FROG);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, first)).isFalse();
    }
}
