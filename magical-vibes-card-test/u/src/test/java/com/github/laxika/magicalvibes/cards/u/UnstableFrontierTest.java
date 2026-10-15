package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableFrontier.class, Forest.class})
class UnstableFrontierTest extends BaseCardTest {

    // Unstable Frontier is added first, so it is permanent index 0 on its controller's battlefield.
    private static final int FRONTIER = 0;
    private static final int ADD_COLORLESS = 0;
    private static final int BECOME_TYPE = 1;

    @Test
    @DisplayName("First ability adds one colorless mana")
    void firstAbilityAddsColorless() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, FRONTIER, ADD_COLORLESS, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability puts it on the stack targeting a land you control")
    void secondAbilityTargetsOwnLand() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, forestId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Chosen type replaces the land's subtype and mana (rule 305.7)")
    void chosenTypeOverridesSubtypesAndMana() {
        Permanent forest = becomeIsland();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();

        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.tapPermanent(player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        Permanent forest = becomeIsland();
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(forest.getTransientLandTypeOverride()).isNull();
        harness.forceActivePlayer(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a land controlled by the opponent")
    void cannotTargetOpponentLand() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @ParameterizedTest
    @CsvSource({"PLAINS,WHITE", "ISLAND,BLUE", "SWAMP,BLACK", "MOUNTAIN,RED", "FOREST,GREEN"})
    @DisplayName("Each basic land type grants its mana ability and removes printed abilities")
    void eachBasicLandTypeReplacesPrintedAbilities(String landType, ManaColor color) {
        harness.addToBattlefield(player1, new UnstableFrontier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UnstableFrontier());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, landType);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, BECOME_TYPE, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BloodMoon.class})
    @DisplayName("A later Blood Moon replaces the earlier chosen land type")
    void laterBloodMoonOverridesChosenType() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UnstableFrontier());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        harness.castFromHand(player1, new BloodMoon(), "{2}{R}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Unstable Frontier can target itself and regains its abilities after cleanup")
    void canTargetItself() {
        Permanent frontier = harness.addToBattlefieldAndReturn(player1, new UnstableFrontier());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, frontier.getId());
        assertThat(frontier.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        assertThat(gqs.hasLostPrintedAbilities(gd, frontier)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, FRONTIER, ADD_COLORLESS, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    /** Makes player1 control a Forest that becomes an Island via Unstable Frontier's second ability. */
    private Permanent becomeIsland() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = forest.getId();

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        return forest;
    }
}
