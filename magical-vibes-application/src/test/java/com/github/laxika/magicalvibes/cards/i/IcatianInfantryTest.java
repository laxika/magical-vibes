package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcatianInfantry.class})
class IcatianInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants first strike until end of turn")
    void grantsFirstStrikeUntilEndOfTurn() {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isFalse();
        assertThat(infantry.isTapped()).isFalse();

        expireTemporaryAbilities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The second ability grants banding until end of turn")
    void grantsBandingUntilEndOfTurn() {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(infantry.isTapped()).isFalse();

        expireTemporaryAbilities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Both abilities can be active on the same creature")
    void canGainBothKeywordsAtOnce() {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isTrue();
        assertThat(infantry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each ability affects only the Icatian Infantry that activated it")
    void abilitiesAffectOnlyTheirSource() {
        Permanent firstInfantry = addCreatureReady(player1, new IcatianInfantry());
        Permanent secondInfantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstInfantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstInfantry, Keyword.BANDING)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondInfantry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondInfantry, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Both abilities require one generic mana")
    void abilitiesRequireGenericMana() {
        addCreatureReady(player1, new IcatianInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void canActivateWhileTappedAndSummoningSick(int abilityIndex) {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new IcatianInfantry());
        infantry.setSummoningSick(true);
        infantry.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();

        Keyword keyword = abilityIndex == 0 ? Keyword.FIRST_STRIKE : Keyword.BANDING;
        assertThat(gqs.hasKeyword(gd, infantry, keyword)).isTrue();
        assertThat(infantry.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void keywordIsGrantedOnlyAfterAbilityResolves(int abilityIndex) {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Keyword keyword = abilityIndex == 0 ? Keyword.FIRST_STRIKE : Keyword.BANDING;

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(gqs.hasKeyword(gd, infantry, keyword)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, keyword)).isTrue();
    }

    @Test
    void gainedFirstStrikeKillsBlockerBeforeItCanDealDamage() {
        Permanent attacker = addCreatureReady(player1, new IcatianInfantry());
        Permanent blocker = addCreatureReady(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void gainedBandingMakesBlockingOneMemberBlockBoth() {
        Permanent bander = addCreatureReady(player1, new IcatianInfantry());
        Permanent companion = addCreatureReady(player1, new IcatianInfantry());
        Permanent blocker = addCreatureReady(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(bander.getBandId()).isNotNull().isEqualTo(companion.getBandId());
        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(bander.getId(), companion.getId());
    }

    private void expireTemporaryAbilities() {
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);
    }
}
