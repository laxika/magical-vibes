package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudsteelKirin.class, BearerOfMemory.class, EnchantedEvening.class})
class CloudsteelKirinTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsFlyingAndGameProtection() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        kirin.setAttachedTo(creature.getId());

        assertThat(gqs.isCreature(gd, kirin)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseGame(gd, player2.getId())).isTrue();
        assertThat(gqs.playerHasCantWinGameEffect(gd, player1.getId())).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheKirin() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, kirin)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, kirin)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent opponentCreature = addCreatureReady(player2, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kirin.getAttachedTo()).isNull();
    }

    @Test
    void unattachedKirinDoesNotProtectItsController() {
        addCreatureReady(player1, new CloudsteelKirin());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void equippedCreatureControllerCanWinWhenOpponentLoses() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        kirin.setAttachedTo(creature.getId());
        harness.setLife(player2, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void reconfigureBetweenCreaturesAtZeroLifePreservesProtection() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent first = addCreatureReady(player1, new BearerOfMemory());
        Permanent second = addCreatureReady(player1, new BearerOfMemory());
        kirin.setAttachedTo(first.getId());
        harness.setLife(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void unattachingAtZeroLifeEndsProtection() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        kirin.setAttachedTo(creature.getId());
        harness.setLife(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        kirin.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kirin.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigureCannotAttachKirinToItself() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, kirin.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(kirin.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, kirin)).isTrue();
    }

    @Test
    void reconfigureRequiresFiveMana() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kirin.getAttachedTo()).isNull();
    }

    @Test
    void unattachAbilityRequiresAnAttachedKirin() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kirin.getAttachedTo()).isNull();
    }

    @Test
    void equippedCreatureProtectsControllerFromPoisonLoss() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        kirin.setAttachedTo(creature.getId());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void attachedKirinRetainsOtherCardTypes() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, kirin)).isFalse();
        assertThat(gqs.isEnchantment(gd, kirin)).isTrue();
    }
}
