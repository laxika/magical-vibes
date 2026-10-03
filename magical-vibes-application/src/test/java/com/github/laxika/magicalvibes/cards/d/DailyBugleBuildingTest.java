package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EzekielSimsSpiderTotem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DailyBugleBuilding.class, DailyBugleReporters.class, EzekielSimsSpiderTotem.class})
class DailyBugleBuildingTest extends BaseCardTest {

    @Test
    void manaAbilitiesProduceColorlessAndAnyColorMana() {
        Permanent building = harness.addToBattlefieldAndReturn(player1, new DailyBugleBuilding());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        building.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void smearCampaignGrantsMenaceUntilEndOfTurnToLegendaryCreature() {
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void smearCampaignCannotTargetNonlegendaryCreature() {
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DailyBugleReporters());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    void smearCampaignRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void smearCampaignCanTargetOpponentsLegendaryCreatureAndPaysCostsBeforeResolving() {
        Permanent building = harness.addToBattlefieldAndReturn(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EzekielSimsSpiderTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 2, null, target.getId());

        assertThat(building.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void smearCampaignCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    void smearCampaignCannotBeActivatedWithNonemptyStack() {
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceSorcerySpeed(player1);
        harness.activateAbility(player1, 0, 2, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void smearCampaignDoesNotResolveWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EzekielSimsSpiderTotem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceSorcerySpeed(player1);
        harness.activateAbility(player1, 0, 2, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void filteringManaAbilityProducesEachColorImmediatelyAndPaysItsCosts() {
        Permanent building = harness.addToBattlefieldAndReturn(player1, new DailyBugleBuilding());

        for (ManaColor color : new ManaColor[]{ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN}) {
            building.untap();
            gd.playerManaPools.get(player1.getId()).clear();
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(building.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void smearCampaignRequiresManaAndAnUntappedBuilding() {
        Permanent building = harness.addToBattlefieldAndReturn(player1, new DailyBugleBuilding());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(building.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    private void forceSorcerySpeed(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
