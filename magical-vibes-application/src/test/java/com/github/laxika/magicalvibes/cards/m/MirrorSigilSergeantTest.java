package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorSigilSergeant.class, FugitiveWizard.class})
class MirrorSigilSergeantTest extends BaseCardTest {

    private Permanent addReadySergeant(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MirrorSigilSergeant());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addBluePermanent(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FugitiveWizard());
        perm.setSummoningSick(false);
    }

    private long sergeantCount(Player player) {
        return countPermanents(player, "Mirror-Sigil Sergeant");
    }

    @Test
    @DisplayName("Creates a token copy when controlling a blue permanent and accepting the may")
    void createsTokenCopyWhenAccepting() {
        addReadySergeant(player1);
        addBluePermanent(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability -> queues may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sergeantCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("No token copy when declining the may")
    void noTokenWhenDeclining() {
        addReadySergeant(player1);
        addBluePermanent(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability -> queues may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sergeantCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without a blue permanent (intervening if)")
    void doesNotTriggerWithoutBluePermanent() {
        addReadySergeant(player1);
        // No blue permanent controlled.

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(sergeantCount(player1)).isEqualTo(1);
    }

    @Test
    void opponentsBluePermanentDoesNotEnableTrigger() {
        addReadySergeant(player1);
        addBluePermanent(player2);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(sergeantCount(player1)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        addReadySergeant(player1);
        addBluePermanent(player1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(sergeantCount(player1)).isEqualTo(1);
    }

    @Test
    void losingBluePermanentBeforeResolutionPreventsCopy() {
        addReadySergeant(player1);
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(blue);
        gd.playerGraveyards.get(player1.getId()).add(blue.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sergeantCount(player1)).isEqualTo(1);
    }

    @Test
    void abilityCreatesCopyAfterSergeantLeavesBattlefield() {
        Permanent sergeant = addReadySergeant(player1);
        addBluePermanent(player1);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(sergeant);
        gd.playerGraveyards.get(player1.getId()).add(sergeant.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sergeantCount(player1)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Mirror-Sigil Sergeant"))
                .allSatisfy(p -> assertThat(p.getCard().isToken()).isTrue());
    }

    @Test
    void tokenCopiesTriggerOnNextUpkeepWithoutTriggeringImmediately() {
        addReadySergeant(player1);
        addBluePermanent(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sergeantCount(player1)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sergeantCount(player1)).isEqualTo(4);
    }
}
