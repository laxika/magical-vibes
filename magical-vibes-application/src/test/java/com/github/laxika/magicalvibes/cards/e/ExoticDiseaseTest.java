package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BloodletterOfAclazotz;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodletterOfAclazotz.class, ExoticDisease.class, Forest.class, Island.class, Mountain.class,
        Plains.class, Swamp.class})
class ExoticDiseaseTest extends BaseCardTest {

    private void castAt(Player target) {
        harness.setHand(player1, List.of(new ExoticDisease()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Domain 2: target player loses 2 life and the caster gains 2 life")
    void losesAndGainsLifePerBasicLandType() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        castAt(player2);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once")
    void duplicateTypesCountOnce() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        castAt(player2);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Controlling no basic land types makes the spell have no life effect")
    void noBasicLandTypesDoesNothing() {
        harness.addToBattlefield(player2, new Plains());

        castAt(player2);

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("All five distinct basic land types count toward domain")
    void allFiveBasicLandTypesCountOnce() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        castAt(player2);

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetCaster() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        castAt(player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gain is based on Domain X, not modified life loss")
    void gainsDomainAmountWhenOpponentLifeLossIsDoubled() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        castAt(player2);

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Domain uses the lands controlled at resolution, not at casting")
    void domainIsEvaluatedAtResolution() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new ExoticDisease()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, player2.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof Island);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Self-targeting at one life gains life before state-based actions")
    void selfTargetingDoesNotLoseGameDuringResolution() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        castAt(player1);

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
