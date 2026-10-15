package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntersBlowgun.class, GrizzlyBears.class})
class HuntersBlowgunTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndDeathtouchDuringYourTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blowgun = harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void equippedCreatureHasReachInsteadOfDeathtouchDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blowgun = harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void conditionalKeywordsChangeWhenActivePlayerChanges() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blowgun = harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void equipAttachesAndReequipMovesAllBonuses() {
        Permanent blowgun = harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(blowgun.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blowgun.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void keywordsFollowEquipmentControllerRatherThanCreatureController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent blowgun = harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        blowgun.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void unattachedEquipmentDoesNotGrantBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());

        for (var activePlayer : java.util.List.of(player1, player2)) {
            harness.forceActivePlayer(activePlayer);
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        }
    }
    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new HuntersBlowgun());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
