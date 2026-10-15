package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuyInTheChair.class, GiantSpider.class, GrizzlyBears.class, Lignify.class, ArtificialEvolution.class})
class GuyInTheChairTest extends BaseCardTest {

    @Test
    void tapsForManaOfAnyColor() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(guy.isTapped()).isTrue();
    }

    @Test
    void putsCounterOnTargetSpider() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guy.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonSpiderCreature() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guy.isTapped()).isFalse();
    }

    @Test
    void counterAbilityRequiresSorcerySpeed() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(guy.isTapped()).isFalse();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canPutCounterOnOpponentsSpider() {
        addCreatureReady(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateCounterAbilityWhileAnotherAbilityIsOnStack() {
        addCreatureReady(player1, new GuyInTheChair());
        Permanent second = addCreatureReady(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, 1, null, spider.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(second.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void summoningSicknessPreventsBothTapAbilities() {
        Permanent guy = harness.addToBattlefieldAndReturn(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(guy.isTapped()).isFalse();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manaAbilityCanBeUsedDuringUpkeepAndResolvesWithoutUsingStack() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(guy.isTapped()).isTrue();
    }

    @Test
    void canTargetNoncreatureSpiderPermanent() {
        addCreatureReady(player1, new GuyInTheChair());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lignify(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Lignify");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TREEFOLK");
        harness.handleListChoice(player1, "SPIDER");
        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.SPIDER)).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
