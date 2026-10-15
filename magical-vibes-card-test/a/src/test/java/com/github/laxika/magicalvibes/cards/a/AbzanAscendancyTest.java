package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbzanAscendancy.class, GrizzlyBears.class, WrathOfGod.class})
class AbzanAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a +1/+1 counter on each creature you control")
    void enteringPutsCountersOnOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveAscendancy();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A nontoken creature you control dying creates a Spirit token with flying")
    void ownNontokenCreatureDeathCreatesSpirit() {
        harness.addToBattlefield(player1, new AbzanAscendancy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreaturesWithWrath(player2);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("An opponent's nontoken creature dying does not trigger it")
    void opponentNontokenCreatureDeathDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new AbzanAscendancy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreaturesWithWrath(player1);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveAscendancy() {
        harness.castFromHand(player1, new AbzanAscendancy(), "{W}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyCreaturesWithWrath(com.github.laxika.magicalvibes.model.Player caster) {
        harness.forceActivePlayer(caster);
        harness.castFromHand(caster, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering without creatures puts no counters on the enchantment")
    void enteringWithoutCreaturesDoesNotPutCountersOnEnchantment() {
        castAndResolveAscendancy();

        assertThat(findPermanent(player1, "Abzan Ascendancy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry trigger puts counters on all creatures present when it resolves")
    void entryTriggerUsesCreaturesPresentAtResolution() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new AbzanAscendancy(), "{W}{B}{G}");
        harness.passBothPriorities();
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveAllTriggers();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(laterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Simultaneous nontoken deaths create one Spirit for each controlled creature")
    void simultaneousDeathsCreateSeparateSpirits() {
        harness.addToBattlefield(player1, new AbzanAscendancy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreaturesWithWrath(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Spirit token dying does not create another Spirit")
    void tokenDeathDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new AbzanAscendancy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        destroyCreaturesWithWrath(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);

        destroyCreaturesWithWrath(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
