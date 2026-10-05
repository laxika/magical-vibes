package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.o.Oraxid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Infiltrate.class, Oraxid.class})
class InfiltrateTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Infiltrate makes the target creature unable to be blocked this turn")
    void resolvingMakesTargetUnblockable() {
        harness.addToBattlefield(player1, new Oraxid());
        harness.setHand(player1, List.of(new Infiltrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Oraxid");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent oraxid = findPermanent(player1, "Oraxid");
        assertThat(oraxid.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Infiltrate's unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Oraxid());
        harness.setHand(player1, List.of(new Infiltrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Oraxid");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent oraxid = findPermanent(player1, "Oraxid");
        assertThat(oraxid.isCantBeBlocked()).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(oraxid.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The targeted creature deals combat damage without being blocked")
    void targetedCreatureDealsCombatDamageUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new Oraxid());
        addCreatureReady(player2, new Oraxid());
        harness.setHand(player1, List.of(new Infiltrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Infiltrate can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new Oraxid());
        addCreatureReady(player1, new Oraxid());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Infiltrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Infiltrate cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Infiltrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the targeted attacker cannot be blocked")
    void onlyTargetedAttackerCannotBeBlocked() {
        Permanent target = addCreatureReady(player1, new Oraxid());
        Permanent other = addCreatureReady(player1, new Oraxid());
        addCreatureReady(player2, new Oraxid());
        harness.setHand(player1, List.of(new Infiltrate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, other)).isFalse();
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Oraxid");
        harness.assertOnBattlefield(player2, "Oraxid");
    }
}
