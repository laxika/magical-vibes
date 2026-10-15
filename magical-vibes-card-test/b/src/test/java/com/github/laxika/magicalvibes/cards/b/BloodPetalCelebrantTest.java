package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodPetalCelebrant.class, WrathOfGod.class})
class BloodPetalCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent celebrant = addCreatureReady(player1, new BloodPetalCelebrant());

        celebrant.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, celebrant, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No first strike when not attacking")
    void noFirstStrikeWhenNotAttacking() {
        Permanent celebrant = addCreatureReady(player1, new BloodPetalCelebrant());

        assertThat(gqs.hasKeyword(gd, celebrant, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("When Blood Petal Celebrant dies, a Blood token is created")
    void deathCreatesBloodToken() {
        harness.addToBattlefield(player1, new BloodPetalCelebrant());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        List<Permanent> bloods = findPermanents(player1, "Blood");
        assertThat(bloods).hasSize(1);
        Permanent blood = bloods.getFirst();
        assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getCard().isToken()).isTrue();
        harness.assertInGraveyard(player1, "Blood Petal Celebrant");
    }

    @Test
    @DisplayName("Loses first strike when it stops attacking")
    void losesFirstStrikeWhenItStopsAttacking() {
        Permanent celebrant = addCreatureReady(player1, new BloodPetalCelebrant());
        celebrant.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, celebrant, Keyword.FIRST_STRIKE)).isTrue();

        celebrant.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, celebrant, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attacking Celebrant kills a blocking Celebrant before it can deal damage")
    void firstStrikeKillsBlockerBeforeNormalDamage() {
        Permanent attacker = addCreatureReady(player1, new BloodPetalCelebrant());
        Permanent blocker = addCreatureReady(player2, new BloodPetalCelebrant());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInGraveyard(player2, "Blood Petal Celebrant");
        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Celebrant dying simultaneously creates Blood for its own controller")
    void simultaneousDeathsCreateBloodForEachController() {
        harness.addToBattlefield(player1, new BloodPetalCelebrant());
        harness.addToBattlefield(player1, new BloodPetalCelebrant());
        harness.addToBattlefield(player2, new BloodPetalCelebrant());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
        assertThat(countPermanents(player2, "Blood")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Blood Petal Celebrant");
        harness.assertInGraveyard(player2, "Blood Petal Celebrant");
    }
}
