package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShardingSphinxTest extends BaseCardTest {

    private Permanent addReadySphinx() {
        Permanent perm = new Permanent(new ShardingSphinx());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifactCreature() {
        Permanent perm = new Permanent(new IronMyr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyNonArtifactCreature() {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
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
        Permanent myr = addReadyArtifactCreature();
        myr.setAttacking(true);
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
        Permanent myr = addReadyArtifactCreature();
        myr.setAttacking(true);
        harness.setLife(player2, 20);

        runCombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(thopterTokens()).isZero();
    }

    @Test
    @DisplayName("A non-artifact creature dealing combat damage does not trigger Sharding Sphinx")
    void nonArtifactDoesNotTrigger() {
        addReadySphinx();
        Permanent bears = addReadyNonArtifactCreature();
        bears.setAttacking(true);
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
}
