package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new AbzanAscendancy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyCreaturesWithWrath(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new WrathOfGod()));
        harness.addMana(caster, ManaColor.WHITE, 4);
        harness.forceActivePlayer(caster);

        harness.getGameService().playCard(harness.getGameData(), caster, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
