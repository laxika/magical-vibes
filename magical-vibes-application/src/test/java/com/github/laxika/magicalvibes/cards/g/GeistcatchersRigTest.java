package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.r.ReaperFromTheAbyss;
import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistcatchersRig.class, AbbeyGriffin.class, ReaperFromTheAbyss.class, VictimOfNight.class})
class GeistcatchersRigTest extends BaseCardTest {

    private void castRig() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GeistcatchersRig(), "{6}");
        harness.passBothPriorities();
    }

    private void castRigAndAcceptMay(UUID targetId) {
        castRig();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("ETB deals 4 damage killing a creature with flying")
    void etbKillsFlyingCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());

        castRigAndAcceptMay(griffin.getId());

        harness.assertNotOnBattlefield(player2, "Abbey Griffin");
        harness.assertInGraveyard(player2, "Abbey Griffin");
    }

    @Test
    @DisplayName("Declining the may ability leaves the flying creature unharmed")
    void declineMayLeavesCreatureAlive() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());

        castRig();
        harness.handlePermanentChosen(player1, griffin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Abbey Griffin");
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Geistcatcher's Rig");
    }

    @Test
    @DisplayName("ETB deals exactly four damage to a surviving flyer")
    void etbDealsExactlyFourDamage() {
        Permanent demon = harness.addToBattlefieldAndReturn(player2, new ReaperFromTheAbyss());

        castRigAndAcceptMay(demon.getId());

        harness.assertOnBattlefield(player2, "Reaper from the Abyss");
        assertThat(demon.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB can target a flying creature controlled by its controller")
    void etbCanTargetOwnFlyingCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());

        castRigAndAcceptMay(griffin.getId());

        harness.assertNotOnBattlefield(player1, "Abbey Griffin");
        harness.assertInGraveyard(player1, "Abbey Griffin");
    }

    @Test
    @DisplayName("Without a flying creature, entry completes without a target or may prompt")
    void etbWithNoLegalTarget() {
        castRig();

        harness.assertOnBattlefield(player1, "Geistcatcher's Rig");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB rejects a nonflying creature while a legal flying target exists")
    void etbRejectsNonflyingCreature() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());
        castRig();
        UUID rigId = harness.getPermanentId(player1, "Geistcatcher's Rig");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, rigId))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, griffin.getId());
        assertThat(griffin.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Abbey Griffin");
    }

    @Test
    @DisplayName("ETB does not resolve when its target leaves the battlefield in response")
    void targetLeavesBeforeResolution() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());
        castRig();
        harness.handlePermanentChosen(player1, griffin.getId());

        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, griffin.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Abbey Griffin");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Geistcatcher's Rig");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB still deals damage after Geistcatcher's Rig leaves the battlefield")
    void sourceLeavesBeforeResolution() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());
        castRig();
        harness.handlePermanentChosen(player1, griffin.getId());

        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Geistcatcher's Rig"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Geistcatcher's Rig");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Abbey Griffin");
        harness.assertInGraveyard(player2, "Abbey Griffin");
    }
}
