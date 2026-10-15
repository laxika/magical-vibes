package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShardingSphinx.class, EtheriumSculptor.class, CylianElf.class})
class ShardingSphinxTest extends BaseCardTest {

    private Permanent addReadySphinx() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ShardingSphinx());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyArtifactCreature() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new EtheriumSculptor());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyNonArtifactCreature() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        perm.setSummoningSick(false);
        return perm;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage → triggers onto stack
        harness.passBothPriorities(); // resolve the ally-combat-damage trigger (MayEffect prompt)
    }

    private long thopterTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Thopter"))
                .count();
    }

    @Test
    @DisplayName("Accepting the may ability creates a 1/1 blue Thopter artifact creature token with flying")
    void artifactCombatDamageCreatesThopter() {
        addReadySphinx();
        Permanent sculptor = addReadyArtifactCreature();
        sculptor.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Thopter"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may ability creates no token")
    void decliningCreatesNoToken() {
        addReadySphinx();
        Permanent sculptor = addReadyArtifactCreature();
        sculptor.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(thopterTokens()).isZero();
    }

    @Test
    @DisplayName("A non-artifact creature dealing combat damage does not trigger Sharding Sphinx")
    void nonArtifactDoesNotTrigger() {
        addReadySphinx();
        Permanent elf = addReadyNonArtifactCreature();
        elf.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(thopterTokens()).isZero();
    }

    @Test
    @DisplayName("Sharding Sphinx triggers for itself when it deals combat damage")
    void triggersForItself() {
        Permanent sphinx = addReadySphinx();
        sphinx.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(thopterTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("The created token is blue and enters as an untapped nonattacking creature")
    void tokenEntersAsBlueCreatureOutsideCombat() {
        addReadySphinx().setAttacking(true);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Thopter"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Each artifact creature dealing damage triggers independently and may be declined separately")
    void multipleArtifactCreaturesTriggerSeparately() {
        addReadySphinx();
        addReadyArtifactCreature().setAttacking(true);
        addReadyArtifactCreature().setAttacking(true);

        runCombatDamage();
        harness.assertLife(player2, 18);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(thopterTokens()).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(thopterTokens()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Sharding Sphinx triggers once for the same artifact creature")
    void multipleSphinxesEachCreateToken() {
        addReadySphinx();
        addReadySphinx();
        addReadyArtifactCreature().setAttacking(true);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thopterTokens()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact creature does not trigger Sharding Sphinx")
    void opponentArtifactCreatureDoesNotTrigger() {
        addReadySphinx();
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new EtheriumSculptor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(thopterTokens()).isZero();
    }

    @Test
    @DisplayName("A triggered ability still creates its token after Sharding Sphinx leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent sphinx = addReadySphinx();
        addReadyArtifactCreature().setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(sphinx);
        gd.playerGraveyards.get(player1.getId()).add(sphinx.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thopterTokens()).isEqualTo(1);
    }
}
