package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WebShot;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ettercap.class, WebShot.class, WindDrake.class, GrizzlyBears.class})
class EttercapTest extends BaseCardTest {

    @Test
    void adventureDestroysFlyingCreatureAndExilesTheCard() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Ettercap card = new Ettercap();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, drake.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(drake);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetCreatureWithoutFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Ettercap card = new Ettercap();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Ettercap card = new Ettercap();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, drake.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ettercap");
    }

    @Test
    void adventureCanDestroyItsControllersFlyingCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Ettercap card = new Ettercap();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, drake.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertInGraveyard(player1, "Wind Drake");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardWithoutCastPermission() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Ettercap card = new Ettercap();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, drake.getId());
        gd.playerBattlefields.get(player2.getId()).remove(drake);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void creatureCanBeCastDirectlyWithoutDestroyingFlyingCreatures() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Ettercap card = new Ettercap();

        harness.castFromHand(player1, card, "{4}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ettercap");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(drake);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new WindDrake());
        Permanent ettercap = addCreatureReady(player2, new Ettercap());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(ettercap.isBlocking()).isTrue();
    }
}
