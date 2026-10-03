package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleaverRiot.class, GrizzlyBears.class, JayemdaeTome.class})
class CleaverRiotTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants double strike to all creatures you control")
    void grantsDoubleStrikeToOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);
        for (Permanent p : battlefield) {
            assertThat(p.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
        }
    }

    @Test
    @DisplayName("Does not grant double strike to opponent's creatures")
    void doesNotGrantToOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
        }
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
        }
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cleaver Riot");
    }

    @Test
    @DisplayName("Creatures entering before resolution gain double strike")
    void includesCreaturesEnteringBeforeResolution() {
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 0);

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain double strike")
    void excludesCreaturesEnteringAfterResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Noncreature permanents do not gain double strike")
    void excludesNoncreaturePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new JayemdaeTome());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted double strike deals both first-strike and regular combat damage")
    void dealsDamageInBothCombatDamageSteps() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, 0);

        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertLife(player2, 18);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 16);
    }
}
