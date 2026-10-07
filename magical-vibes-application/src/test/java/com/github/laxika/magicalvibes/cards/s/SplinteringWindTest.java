package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishBard;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplinteringWind.class, ElvishBard.class, RayOfCommand.class})
class SplinteringWindTest extends BaseCardTest {

    private Permanent splinterToken() {
        return findPermanent(player1, "Splinter");
    }

    private Permanent activateOnTarget(Permanent target) {
        harness.addToBattlefield(player1, new SplinteringWind());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        return splinterToken();
    }

    @Test
    @DisplayName("Ability deals 1 damage to target creature and creates a 1/1 flying Splinter")
    void damagesTargetAndCreatesToken() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());

        Permanent token = activateOnTarget(bard);

        assertThat(bard.getMarkedDamage()).isEqualTo(1);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps the Splinter and adds an age counter")
    void payingCumulativeUpkeepKeepsSplinter() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());
        Permanent token = activateOnTarget(bard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(token.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Declining the Splinter's cumulative upkeep sacrifices it, dealing 1 damage to its controller and each creature they control")
    void unpaidUpkeepSacrificesAndDamages() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());
        Permanent token = activateOnTarget(bard);
        Permanent ownBard = harness.addToBattlefieldAndReturn(player1, new ElvishBard());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(ownBard.getMarkedDamage()).isEqualTo(1);
        assertThat(bard.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leaving the battlefield without being sacrificed still damages its controller and their creatures")
    void leavingTheBattlefieldTriggersDamage() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());
        Permanent token = activateOnTarget(bard);
        Permanent ownBard = harness.addToBattlefieldAndReturn(player1, new ElvishBard());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, token));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(ownBard.getMarkedDamage()).isEqualTo(1);
        assertThat(bard.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A stolen Splinter still damages the player who created it and that player's creatures")
    void stolenSplinterDamagesOriginalAbilityController() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());
        Permanent token = activateOnTarget(bard);
        Permanent ownBard = harness.addToBattlefieldAndReturn(player1, new ElvishBard());
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, token.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, token));
        harness.passBothPriorities();

        harness.assertLife(player1, ownLife - 1);
        harness.assertLife(player2, opponentLife);
        assertThat(ownBard.getMarkedDamage()).isEqualTo(1);
        assertThat(bard.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target prevents both damage and token creation")
    void removedTargetPreventsTokenCreation() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());
        harness.addToBattlefield(player1, new SplinteringWind());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bard.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, bard));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Splinter");
    }

    @Test
    @DisplayName("The second cumulative upkeep requires two green mana")
    void secondUpkeepCostsTwoGreenMana() {
        Permanent bard = harness.addToBattlefieldAndReturn(player2, new ElvishBard());
        Permanent token = activateOnTarget(bard);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(token.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
