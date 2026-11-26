package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenDriller.class, DwarvenScorcher.class, KrosanVerge.class, RiftstonePortal.class})
class DwarvenDrillerTest extends BaseCardTest {

    @Test
    void controllerAcceptsDamageAndLandSurvives() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Riftstone Portal");
        harness.assertLife(player2, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void canTargetOwnLandAndDamageItsController() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Riftstone Portal");
        harness.assertLife(player1, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void controllerDeclinesDamageAndLandIsDestroyed() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Riftstone Portal");
        harness.assertLife(player2, 20);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonlandPermanent() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent scorcher = harness.addToBattlefieldAndReturn(player2, new DwarvenScorcher());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, driller), 0, null, scorcher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent driller = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, driller), 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Creature has summoning sickness");
        assertThat(driller.isTapped()).isFalse();
    }

    @Test
    void targetsItsControllersOwnLand() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Krosan Verge");
        harness.assertLife(player1, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void abilityStillDealsDamageAfterDrillerLeavesBattlefield() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(driller);
        harness.setGraveyard(player1, List.of(driller.getCard()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Dwarven Driller");
        harness.assertOnBattlefield(player2, "Riftstone Portal");
        harness.assertLife(player2, 18);
    }

    @Test
    void missingTargetDoesNotAskControllerToTakeDamage() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.setGraveyard(player2, List.of(land.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Riftstone Portal");
        harness.assertLife(player2, 20);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class})
    @DisplayName("Declining the damage asks the destroyed Dryad Arbor's death replacements before it leaves")
    void decliningAsksTheDestroyedLandsDeathReplacementsFirst() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent arbor = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        harness.addToBattlefieldAndReturn(player2, new NecromancersMagemark()).setAttachedTo(arbor.getId());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, arbor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(arbor);

        harness.handleListChoice(player2, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player2.getId())).contains(arbor.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(arbor);
        harness.assertLife(player2, 20);
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";
}
