package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KillerWhale;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OmenHawker.class, KillerWhale.class, GrizzlyBears.class})
class OmenHawkerTest extends BaseCardTest {

    @Test
    void addsBlueAndColorlessAbilityOnlyMana() {
        addReady(new OmenHawker());

        harness.activateAbility(player1, 0, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.getAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void abilityOnlyManaCanPayForAnActivatedAbility() {
        addReady(new OmenHawker());
        addReady(new KillerWhale());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Killer Whale"), Keyword.FLYING)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void abilityOnlyManaCannotPayForACreatureSpell() {
        addReady(new OmenHawker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
