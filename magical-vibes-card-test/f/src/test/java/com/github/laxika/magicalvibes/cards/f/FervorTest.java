package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArdentMilitia.class, Fervor.class, LlanowarElves.class, Opalescence.class})
class FervorTest extends BaseCardTest {

    @Test
    void newlyControlledCreatureCanAttack() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArdentMilitia());
        creature.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    void newlyControlledCreatureCanTapForMana() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(true);

        harness.tapPermanent(player1, 1);

        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void losingFervorRestoresSummoningSicknessRestriction() {
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(true);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fervor);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elves.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void anotherFervorKeepsGrantingHasteAfterOneLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Fervor());
        harness.addToBattlefield(player1, new Fervor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArdentMilitia());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures you control have haste while Fervor is on the battlefield")
    void ownCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArdentMilitia());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fervor does not grant haste to opponents' creatures")
    void opponentCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new Fervor());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ArdentMilitia());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Fervor does not grant haste to noncreature permanents")
    void noncreaturePermanentsDoNotHaveHaste() {
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());

        assertThat(gqs.isCreature(gd, fervor)).isFalse();
        assertThat(gqs.hasKeyword(gd, fervor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Fervor's haste grant ends when Fervor leaves the battlefield")
    void hasteGrantEndsWhenFervorLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArdentMilitia());
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fervor);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Fervor still grants haste to itself when it becomes a creature")
    void animatedFervorHasHaste() {
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, fervor)).isTrue();
        assertThat(gqs.hasKeyword(gd, fervor, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("An animated Fervor also has haste when it is a creature you control")
    void fervorEnteringAfterOpalescenceHasHaste() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent fervor = harness.addToBattlefieldAndReturn(player1, new Fervor());

        assertThat(gqs.isCreature(gd, fervor)).isTrue();
        assertThat(gqs.hasKeyword(gd, fervor, Keyword.HASTE)).isTrue();
    }
}
