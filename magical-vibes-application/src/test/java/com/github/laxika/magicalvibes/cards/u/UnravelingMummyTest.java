package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnravelingMummy.class, GrizzlyBears.class})
class UnravelingMummyTest extends BaseCardTest {

    private Permanent addMummy() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        mummy.setSummoningSick(false);
        return mummy;
    }

    private void addWhiteMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addBlackMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Grants lifelink to an attacking Zombie, then it wears off at end of turn")
    void grantsLifelinkToAttackingZombie() {
        addMummy();
        Permanent attackingZombie = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        attackingZombie.setSummoningSick(false);
        attackingZombie.setAttacking(true);
        addWhiteMana();

        harness.activateAbility(player1, 0, 0, null, attackingZombie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Grants deathtouch to an attacking Zombie, then it wears off at end of turn")
    void grantsDeathtouchToAttackingZombie() {
        addMummy();
        Permanent attackingZombie = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        attackingZombie.setSummoningSick(false);
        attackingZombie.setAttacking(true);
        addBlackMana();

        harness.activateAbility(player1, 0, 1, null, attackingZombie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a Zombie that is not attacking")
    void cannotTargetNonAttackingZombie() {
        addMummy();
        Permanent idleZombie = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        idleZombie.setSummoningSick(false);
        addWhiteMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idleZombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking non-Zombie creature")
    void cannotTargetAttackingNonZombie() {
        addMummy();
        Permanent attackingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attackingBear.setSummoningSick(false);
        attackingBear.setAttacking(true);
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, attackingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void canTargetItselfWhileTappedAndAttacking(int abilityIndex) {
        Permanent mummy = addMummy();
        mummy.setAttacking(true);
        mummy.tap();
        if (abilityIndex == 0) addWhiteMana();
        else addBlackMana();

        harness.activateAbility(player1, 0, abilityIndex, null, mummy.getId());
        harness.passBothPriorities();

        Keyword keyword = abilityIndex == 0 ? Keyword.LIFELINK : Keyword.DEATHTOUCH;
        assertThat(gqs.hasKeyword(gd, mummy, keyword)).isTrue();
        assertThat(mummy.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void canGrantKeywordToOpponentsAttackingZombieWhileSummoningSick(int abilityIndex) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        source.setSummoningSick(true);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new UnravelingMummy());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        if (abilityIndex == 0) addWhiteMana();
        else addBlackMana();

        harness.activateAbility(player1, 0, abilityIndex, null, attacker.getId());
        harness.passBothPriorities();

        Keyword keyword = abilityIndex == 0 ? Keyword.LIFELINK : Keyword.DEATHTOUCH;
        assertThat(gqs.hasKeyword(gd, attacker, keyword)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, keyword)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void doesNotResolveWhenTargetStopsAttacking(int abilityIndex) {
        Permanent mummy = addMummy();
        mummy.setAttacking(true);
        if (abilityIndex == 0) addWhiteMana();
        else addBlackMana();
        harness.activateAbility(player1, 0, abilityIndex, null, mummy.getId());

        mummy.setAttacking(false);
        harness.passBothPriorities();

        Keyword keyword = abilityIndex == 0 ? Keyword.LIFELINK : Keyword.DEATHTOUCH;
        assertThat(gqs.hasKeyword(gd, mummy, keyword)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void grantedKeywordPersistsAfterTargetStopsAttacking(int abilityIndex) {
        Permanent mummy = addMummy();
        mummy.setAttacking(true);
        if (abilityIndex == 0) addWhiteMana();
        else addBlackMana();
        harness.activateAbility(player1, 0, abilityIndex, null, mummy.getId());
        harness.passBothPriorities();

        mummy.setAttacking(false);

        Keyword keyword = abilityIndex == 0 ? Keyword.LIFELINK : Keyword.DEATHTOUCH;
        assertThat(gqs.hasKeyword(gd, mummy, keyword)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void abilityResolvesAfterSourceLeavesBattlefield(int abilityIndex) {
        Permanent source = addMummy();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        if (abilityIndex == 0) addWhiteMana();
        else addBlackMana();
        harness.activateAbility(player1, 0, abilityIndex, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        Keyword keyword = abilityIndex == 0 ? Keyword.LIFELINK : Keyword.DEATHTOUCH;
        assertThat(gqs.hasKeyword(gd, attacker, keyword)).isTrue();
    }
}
