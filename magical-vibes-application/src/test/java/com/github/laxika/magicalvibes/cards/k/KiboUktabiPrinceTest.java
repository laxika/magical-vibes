package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.z.ZodiacMonkey;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KiboUktabiPrince.class, GrizzlyBears.class, LotusPetal.class, ZodiacMonkey.class,
        KoglaTheTitanApe.class})
class KiboUktabiPrinceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Banana token and the Banana makes mana and gains life")
    void createsAndUsesBanana() {
        Permanent kibo = addCreatureReady(player1, new KiboUktabiPrince());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent banana = findPermanent(player1, "Banana");
        assertThat(banana.getCard().getName()).isEqualTo("Banana");
        assertThat(findPermanent(player2, "Banana")).isNotNull();

        harness.activateAbility(player1, 1, null, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, "RED");
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(banana);
        assertThat(kibo.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters controlled Apes and Monkeys when an opponent artifact is sacrificed")
    void countersApesAndMonkeysFromOpponentArtifact() {
        Permanent kibo = addCreatureReady(player1, new KiboUktabiPrince());
        Permanent ape = addCreatureReady(player1, new KoglaTheTitanApe());
        Permanent monkey = addCreatureReady(player1, new ZodiacMonkey());
        Permanent nonMatchingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LotusPetal());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new LotusPetal());

        kibo.setAttackTarget(player2.getId());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kibo)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(kibo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ape.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(monkey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonMatchingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);
        harness.assertInGraveyard(player2, "Lotus Petal");
    }

    @Test
    @DisplayName("Attack trigger does not require a sacrifice when the defender has no artifact")
    void attackTriggerDoesNothingWithoutArtifact() {
        Permanent kibo = addCreatureReady(player1, new KiboUktabiPrince());
        kibo.setAttackTarget(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kibo)));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
