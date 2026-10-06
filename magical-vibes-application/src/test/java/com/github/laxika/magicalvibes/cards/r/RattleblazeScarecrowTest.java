package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.f.Firespout;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.s.SmashToSmithereens;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RattleblazeScarecrow.class, AshenmoorCohort.class, RustrazorButcher.class,
        SmashToSmithereens.class, Firespout.class, PaintersServant.class})
class RattleblazeScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Has persist while controlling a black creature")
    void hasPersistWithBlackCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player1, new AshenmoorCohort()); // black

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Loses persist when no black creature is controlled")
    void noPersistWithoutBlackCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("An opponent's black creature does not grant persist")
    void opponentBlackCreatureDoesNotGrantPersist() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player2, new AshenmoorCohort()); // opponent's black

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Has haste while controlling a red creature")
    void hasHasteWithRedCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player1, new RustrazorButcher()); // red

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not have haste when no red creature is controlled")
    void noHasteWithoutRedCreature() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player1, new AshenmoorCohort()); // black, not red

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    @Test
    void persistReturnsWithCounterAndDoesNotReturnOnSecondDeath() {
        addCreatureReady(player1, new AshenmoorCohort());
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        harness.setHand(player1, List.of(new SmashToSmithereens(), new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, scarecrow.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Rattleblaze Scarecrow");
        assertThat(returned.getId()).isNotEqualTo(scarecrow.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Rattleblaze Scarecrow");

        harness.castInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Rattleblaze Scarecrow");
        harness.assertInGraveyard(player1, "Rattleblaze Scarecrow");
    }

    @Test
    void deathWithoutBlackCreatureDoesNotReturn() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, scarecrow.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Rattleblaze Scarecrow");
        harness.assertInGraveyard(player1, "Rattleblaze Scarecrow");
    }

    @Test
    void simultaneousDeathOfLastBlackCreatureStillTriggersPersist() {
        addCreatureReady(player1, new AshenmoorCohort());
        addCreatureReady(player1, new RattleblazeScarecrow());
        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ashenmoor Cohort");
        Permanent returned = findPermanent(player1, "Rattleblaze Scarecrow");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.PERSIST)).isFalse();
    }

    @Test
    void summonedScarecrowCanAttackOnlyWhileRedCreatureIsControlled() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new RattleblazeScarecrow());
        scarecrow.setSummoningSick(true);
        assertThat(als.canAttack(gd, scarecrow, player1.getId())).isFalse();

        Permanent redCreature = addCreatureReady(player1, new RustrazorButcher());
        assertThat(als.canAttack(gd, scarecrow, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(redCreature);
        assertThat(als.canAttack(gd, scarecrow, player1.getId())).isFalse();
    }

    @Test
    void opponentsRedCreatureDoesNotGrantHaste() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        addCreatureReady(player2, new RustrazorButcher());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    @Test
    void losesPersistWhenLastBlackCreatureLeaves() {
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());
        Permanent blackCreature = addCreatureReady(player1, new AshenmoorCohort());
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blackCreature);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }

    @Test
    void scarecrowItselfCanSatisfyBlackCreatureCondition() {
        harness.addToBattlefieldAndReturn(player2, new PaintersServant())
                .setChosenColor(CardColor.BLACK);
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    @Test
    void scarecrowItselfCanSatisfyRedCreatureCondition() {
        harness.addToBattlefieldAndReturn(player2, new PaintersServant())
                .setChosenColor(CardColor.RED);
        Permanent scarecrow = addCreatureReady(player1, new RattleblazeScarecrow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.PERSIST)).isFalse();
    }
}
