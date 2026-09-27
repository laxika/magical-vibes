package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SuppressionField;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonGatekeeper.class, BorosSignet.class, ElvesOfDeepShadow.class,
        Forest.class, SuppressionField.class})
class LoxodonGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's artifacts, creatures, and lands enter tapped")
    void opponentsArtifactsCreaturesAndLandsEnterTapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new BorosSignet(), new ElvesOfDeepShadow(), new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Boros Signet").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Elves of Deep Shadow").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's artifacts, creatures, and lands enter untapped")
    void controllersArtifactsCreaturesAndLandsEnterUntapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player1, List.of(new BorosSignet(), new ElvesOfDeepShadow(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Boros Signet").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Elves of Deep Shadow").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's enchantments enter untapped")
    void opponentsEnchantmentsEnterUntapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new SuppressionField()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Suppression Field").isTapped()).isFalse();
    }
}
