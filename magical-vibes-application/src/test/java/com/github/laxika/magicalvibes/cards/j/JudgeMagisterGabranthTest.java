package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JudgeMagisterGabranth.class, Forest.class, GrizzlyBears.class, LotusPetal.class,
        NaturalAffinity.class, Shatter.class, Shock.class, StoneRain.class})
class JudgeMagisterGabranthTest extends BaseCardTest {

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new JudgeMagisterGabranth());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new JudgeMagisterGabranth());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature you control dies")
    void putsCounterWhenAllyCreatureDies() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killPermanent(player1, "Grizzly Bears", player2, new Shock(), ManaColor.RED, 1);
        harness.passBothPriorities();

        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an artifact you control is put into a graveyard")
    void putsCounterWhenAllyArtifactDies() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player1, new LotusPetal());

        killPermanent(player1, "Lotus Petal", player2, new Shatter(), ManaColor.RED, 2);
        harness.passBothPriorities();

        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature nonartifact permanent you control dies")
    void doesNotTriggerWhenAllyLandDies() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player1, new Forest());

        killPermanent(player1, "Forest", player2, new StoneRain(), ManaColor.RED, 3);
        harness.passBothPriorities();

        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killPermanent(player2, "Grizzly Bears", player1, new Shock(), ManaColor.RED, 1);
        harness.passBothPriorities();

        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterWhenAnimatedLandDies() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0);

        killPermanent(player1, "Forest", player2, new Shock(), ManaColor.RED, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new JudgeMagisterGabranth());

        killPermanent(player1, "Judge Magister Gabranth", player2, new Shock(), ManaColor.RED, 1);

        harness.assertInGraveyard(player1, "Judge Magister Gabranth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void putsCounterWhenAllyArtifactIsSacrificed() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player1, new LotusPetal());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lotus Petal");
        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOpponentArtifactDies() {
        Permanent gabranth = harness.addToBattlefieldAndReturn(player1, new JudgeMagisterGabranth());
        harness.addToBattlefield(player2, new LotusPetal());

        killPermanent(player2, "Lotus Petal", player1, new Shatter(), ManaColor.RED, 2);
        harness.passBothPriorities();

        assertThat(gabranth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void killPermanent(Player player, String name, Player caster, Card spell,
            ManaColor manaColor, int manaAmount) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(spell));
        harness.addMana(caster, manaColor, manaAmount);
        UUID permanentId = harness.getPermanentId(player, name);
        if (spell instanceof Shock || spell instanceof Shatter) {
            harness.castAndResolveInstant(caster, 0, permanentId);
        } else {
            harness.castAndResolveSorcery(caster, 0, permanentId);
        }
    }
}
