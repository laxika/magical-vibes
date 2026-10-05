package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousCemetery.class, GrizzlyBears.class})
class OminousCemeteryTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new OminousCemetery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void exilesItselfAndShufflesTargetCreatureIntoItsOwnersLibrary() {
        harness.addToBattlefield(player1, new OminousCemetery());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertNotOnBattlefield(player1, "Ominous Cemetery");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new OminousCemetery());
        harness.addToBattlefield(player2, new OminousCemetery());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player2, "Ominous Cemetery");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shufflesStolenCreatureIntoOwnersLibraryRatherThanControllers() {
        harness.addToBattlefield(player1, new OminousCemetery());
        GrizzlyBears creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, creature);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, permanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ominous Cemetery"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void targetLeavingBattlefieldDoesNotRefundExileOrManaCosts() {
        harness.addToBattlefield(player1, new OminousCemetery());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ominous Cemetery"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateShuffleAbilityWithInsufficientMana() {
        var cemetery = harness.addToBattlefieldAndReturn(player1, new OminousCemetery());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ominous Cemetery");
        assertThat(cemetery.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateShuffleAbilityAfterTappingForMana() {
        harness.addToBattlefield(player1, new OminousCemetery());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ominous Cemetery");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
