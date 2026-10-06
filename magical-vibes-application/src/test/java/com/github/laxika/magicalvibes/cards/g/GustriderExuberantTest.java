package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CavernThoctar;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustriderExuberant.class, CavernThoctar.class, CylianElf.class, WoollyThoctar.class})
class GustriderExuberantTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 5 or greater gain flying; the ability sacrifices the source")
    void grantsFlyingToBigCreatures() {
        addReadyGustrider();
        Permanent big = addReadyCreature(new CavernThoctar());
        big.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent small = addReadyCreature(new CylianElf());  // 2/2

        activateGustrider();

        harness.assertInGraveyard(player1, "Gustrider Exuberant");
        assertThat(big.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(small.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature with exactly power 5 gains flying")
    void grantsFlyingAtPowerFive() {
        addReadyGustrider();
        Permanent thoctar = addReadyCreature(new WoollyThoctar()); // 5/4

        activateGustrider();

        assertThat(thoctar.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures with power 5 or greater do not gain flying")
    void doesNotGrantFlyingToOpponentsCreatures() {
        addReadyGustrider();
        addReadyCreature(new CavernThoctar());
        harness.addToBattlefield(player2, new CavernThoctar());

        activateGustrider();

        assertThat(findPermanent(player1, "Cavern Thoctar").hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(findPermanent(player2, "Cavern Thoctar").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Power is checked only as the ability resolves")
    void powerCheckedOnlyAtResolution() {
        addReadyGustrider();
        Permanent small = addReadyCreature(new CylianElf()); // 2/2
        Permanent big = addReadyCreature(new WoollyThoctar());  // 5/4

        activateGustrider();
        assertThat(small.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(big.hasKeyword(Keyword.FLYING)).isTrue();

        // Later power change does not add or remove the grant (ruling).
        small.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // now 5/5
        big.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3); // now 2/1

        assertThat(small.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(big.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        addReadyGustrider();
        Permanent big = addReadyCreature(new CavernThoctar());

        activateGustrider();
        assertThat(big.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(big.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Power changes before resolution determine which creatures gain flying")
    void checksPowerAfterActivation() {
        harness.addToBattlefield(player1, new GustriderExuberant());
        Permanent growing = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent shrinking = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Gustrider Exuberant");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, shrinking, Keyword.FLYING)).isFalse();

        growing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        shrinking.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, growing, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, shrinking, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain flying")
    void doesNotAffectLaterCreatures() {
        addReadyGustrider();
        Permanent original = addReadyCreature(new WoollyThoctar());
        activateGustrider();

        Permanent later = harness.enterBattlefieldAndReturn(player1, new CavernThoctar());

        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick source can be sacrificed with no eligible creatures")
    void canActivateWithoutEligibleCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GustriderExuberant());
        source.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Gustrider Exuberant");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private void addReadyGustrider() {
        addCreatureReady(player1, new GustriderExuberant());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player1, card);
    }

    private void activateGustrider() {
        harness.activateAbility(player1, indexOf("Gustrider Exuberant"), null, null);
        harness.passBothPriorities();
    }

    private int indexOf(String name) {
        var battlefield = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Not found: " + name);
    }
}
