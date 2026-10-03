package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.m.MirriTheCursed;
import com.github.laxika.magicalvibes.cards.r.RathiTrapper;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DustElemental.class, BloodKnight.class, UrborgTombOfYawgmoth.class,
        MirriTheCursed.class, RathiTrapper.class})
class DustElementalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets you choose exactly three creatures you control, including Dust Elemental")
    void choosesThreeCreaturesToReturn() {
        UUID firstCreatureId = harness.addToBattlefieldAndReturn(player1, new BloodKnight()).getId();
        UUID secondCreatureId = harness.addToBattlefieldAndReturn(player1, new BloodKnight()).getId();
        UUID thirdCreatureId = harness.addToBattlefieldAndReturn(player1, new BloodKnight()).getId();
        harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefieldAndReturn(player2, new BloodKnight());
        harness.castFromHand(player1, new DustElemental(), "{2}{W}{W}");
        resolveAllTriggers();

        UUID dustElementalId = harness.getPermanentId(player1, "Dust Elemental");
        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstCreatureId, secondCreatureId, thirdCreatureId, dustElementalId);
        assertThat(choice.maxCount()).isEqualTo(3);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(firstCreatureId, secondCreatureId)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1,
                List.of(dustElementalId, firstCreatureId, secondCreatureId));

        harness.assertInHand(player1, "Dust Elemental");
        harness.assertInHand(player1, "Blood Knight");
        harness.assertOnBattlefield(player1, "Blood Knight");
        harness.assertOnBattlefield(player1, "Urborg, Tomb of Yawgmoth");
        harness.assertOnBattlefield(player2, "Blood Knight");
    }

    @Test
    @DisplayName("ETB returns all available creatures when you control fewer than three")
    void returnsAllAvailableCreaturesWhenFewerThanThree() {
        harness.castFromHand(player1, new DustElemental(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Dust Elemental");
    }

    @Test
    @DisplayName("Exactly three creatures all return without a choice, despite protection from white")
    void returnsExactlyThreeAvailableCreatures() {
        harness.addToBattlefield(player1, new BloodKnight());
        harness.addToBattlefield(player1, new BloodKnight());

        harness.castFromHand(player1, new DustElemental(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Dust Elemental");
    }

    @Test
    @DisplayName("A controlled creature owned by the opponent returns to that opponent's hand")
    void returnsCreatureToItsOwnersHand() {
        BloodKnight borrowedCreature = new BloodKnight();
        borrowedCreature.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, borrowedCreature);

        harness.castFromHand(player1, new DustElemental(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInHand(player2, "Blood Knight");
        harness.assertNotInHand(player1, "Blood Knight");
        harness.assertInHand(player1, "Dust Elemental");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new DustElemental(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInHand(player1, "Dust Elemental");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blockers must satisfy both flying and fear")
    void flyingAndFearBothRestrictBlocking() {
        Permanent attacker = addCreatureReady(player1, new DustElemental());
        Permanent whiteFlyer = addCreatureReady(player2, new DustElemental());
        Permanent blackGroundCreature = addCreatureReady(player2, new RathiTrapper());
        Permanent blackFlyer = addCreatureReady(player2, new MirriTheCursed());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, whiteFlyer, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackGroundCreature, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackFlyer, attacker, defenders)).isTrue();
    }
}
