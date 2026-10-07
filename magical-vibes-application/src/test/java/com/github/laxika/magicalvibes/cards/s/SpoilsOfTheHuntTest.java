package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpoilsOfTheHunt.class, HillGiant.class, RumblingBaloth.class, WilyGoblin.class,
        YouComeToARiver.class})
class SpoilsOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("The creature deals damage equal to its unboosted power without Treasure mana")
    void dealsDamageUsingUnboostedPowerWithoutTreasureMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        castSpoils(source, victim);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(victim);
    }

    @Test
    @DisplayName("Boosts the creature and uses the boosted power for each Treasure mana spent")
    void boostsAndDealsDamageForTreasureManaSpent() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());

        castWithTwoTreasureMana(source, victim);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(5);
        harness.assertInGraveyard(player2, "Rumbling Baloth");
    }

    @Test
    @DisplayName("The temporary boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        castWithTreasureMana(source, victim);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
    }

    @Test
    void oneTreasureBoostsOnlyPowerAndDamageIsOneWay() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());

        castWithTreasureMana(source, victim);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Rumbling Baloth");
    }

    @Test
    void stillBoostsSourceWhenVictimLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        prepareTreasureMana();
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));

        returnTargetInResponse(victim);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        harness.assertInHand(player2, "Rumbling Baloth");
        harness.assertNotInGraveyard(player2, "Rumbling Baloth");
        harness.assertInGraveyard(player1, "Spoils of the Hunt");
    }

    @Test
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        harness.setHand(player1, List.of(new SpoilsOfTheHunt()));
        addMana();
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));

        returnTargetInResponse(source);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(victim);
        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Spoils of the Hunt");
    }

    private void returnTargetInResponse(Permanent target) {
        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Both targets must have the required controller")
    void rejectsTargetsWithWrongController() {
        Permanent ownSource = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent ownVictim = harness.addToBattlefieldAndReturn(player1, new RumblingBaloth());
        harness.setHand(player1, List.of(new SpoilsOfTheHunt()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownSource.getId(), ownVictim.getId())))
                .isInstanceOf(IllegalStateException.class);

        Permanent opponentSource = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentVictim = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        harness.setHand(player1, List.of(new SpoilsOfTheHunt()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opponentSource.getId(), opponentVictim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSpoils(Permanent source, Permanent victim) {
        harness.setHand(player1, List.of(new SpoilsOfTheHunt()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));
    }

    private void castWithTreasureMana(Permanent source, Permanent victim) {
        prepareTreasureMana();
        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));
    }

    private void prepareTreasureMana() {
        harness.setHand(player1, List.of(new WilyGoblin(), new SpoilsOfTheHunt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
    }

    private void castWithTwoTreasureMana(Permanent source, Permanent victim) {
        harness.setHand(player1, List.of(new WilyGoblin(), new WilyGoblin(), new SpoilsOfTheHunt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        sacrificeTreasureForColorless();
        sacrificeTreasureForColorless();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));
    }

    private void sacrificeTreasureForColorless() {
        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
