package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaredevilFearlessFighter.class, GrizzlyBears.class, LightningBolt.class, Mountain.class})
class DaredevilFearlessFighterTest extends BaseCardTest {

    @Test
    void controlledSourceDamageTriggersDamageToTargetOpponent() {
        harness.addToBattlefield(player1, new DaredevilFearlessFighter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void damageFromOpponentSourceDoesNotTrigger() {
        harness.addToBattlefield(player1, new DaredevilFearlessFighter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void attackingExilesTopCardAndDealsItsManaValueToControllerThenReflectsIt() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilFearlessFighter());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(daredevil);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void attackDamageIsDealtDuringTheSameResolutionAsExiling() {
        addCreatureReady(player1, new DaredevilFearlessFighter());
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void attackWithEmptyLibraryDealsNoDamage() {
        addCreatureReady(player1, new DaredevilFearlessFighter());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exiledInstantCanBeCastThisTurnButStillRequiresMana() {
        addCreatureReady(player1, new DaredevilFearlessFighter());
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);

        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void exiledLandDealsNoDamageAndCanBePlayedInPostcombatMain() {
        addCreatureReady(player1, new DaredevilFearlessFighter());
        Mountain topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void permissionToPlayExiledCardExpiresAfterTheTurn() {
        addCreatureReady(player1, new DaredevilFearlessFighter());
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.passUntil(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void attackAbilityStillExilesAndDealsDamageAfterDaredevilIsRemoved() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilFearlessFighter());
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.castInstant(player2, 0, daredevil.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(daredevil);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void damageTriggerCannotTargetItsController() {
        harness.addToBattlefield(player1, new DaredevilFearlessFighter());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }
}
