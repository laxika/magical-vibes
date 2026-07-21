package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GustriderExuberantTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 5 or greater gain flying; the ability sacrifices the source")
    void grantsFlyingToBigCreatures() {
        addReadyGustrider();
        Permanent big = addReadyCreature(new AvatarOfMight());   // 8/8
        Permanent small = addReadyCreature(new GrizzlyBears());  // 2/2

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
        addReadyCreature(new AvatarOfMight());
        harness.addToBattlefield(player2, new AvatarOfMight());

        activateGustrider();

        assertThat(findPermanent(player1, "Avatar of Might").hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(findPermanent(player2, "Avatar of Might").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Power is checked only as the ability resolves")
    void powerCheckedOnlyAtResolution() {
        addReadyGustrider();
        Permanent small = addReadyCreature(new GrizzlyBears()); // 2/2
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
        Permanent big = addReadyCreature(new AvatarOfMight());

        activateGustrider();
        assertThat(big.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(big.hasKeyword(Keyword.FLYING)).isFalse();
    }

    // ===== Helpers =====

    private void addReadyGustrider() {
        harness.addToBattlefield(player1, new GustriderExuberant());
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gustrider Exuberant"))
                .forEach(p -> p.setSummoningSick(false));
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(player1, card);
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals(card.getName()))
                .findFirst().orElseThrow();
        perm.setSummoningSick(false);
        return perm;
    }

    private void activateGustrider() {
        int index = indexOf("Gustrider Exuberant");
        harness.activateAbility(player1, index, null, null);
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
