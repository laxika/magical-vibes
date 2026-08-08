package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MakeAStandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives own creatures +1/+0 and indestructible")
    void boostsAndGrantsIndestructible() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        for (Permanent p : creaturesOf(player1.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(3);
            assertThat(p.getEffectiveToughness()).isEqualTo(2);
            assertThat(p.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        }
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        for (Permanent p : creaturesOf(player2.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(2);
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        }
    }

    @Test
    @DisplayName("A boosted creature survives lethal damage")
    void indestructibleCreatureSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creaturesOf(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        assertThat(creaturesOf(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent p : creaturesOf(player1.getId())) {
            assertThat(p.getPowerModifier()).isEqualTo(0);
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        }
    }

    private List<Permanent> creaturesOf(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .toList();
    }
}
