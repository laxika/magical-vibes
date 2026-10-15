package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Squirrelanoids;
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

@CardUsed({MutagenManLivingOoze.class, Squirrelanoids.class})
class MutagenManLivingOozeTest extends BaseCardTest {

    @Test
    void createsXMutagenTokens() {
        castMutagenMan(2);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList())
                .hasSize(2);
    }

    @Test
    void reducesMutagenActivationCostAndPutsCounterOnTargetCreature() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Squirrelanoids());
        mutagen.untap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mutagen);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotActivateMutagenOutsideSorcerySpeed() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Squirrelanoids());
        mutagen.untap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void createsNoMutagensWhenXIsZero() {
        castMutagenMan(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertOnBattlefield(player1, "Mutagen Man, Living Ooze");
    }

    @Test
    void mutagenCanTargetAnOpponentsCreatureOnTheTurnItIsCreated() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MutagenManLivingOoze());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, opponentCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mutagen);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCostsOneManaAfterMutagenManLeavesTheBattlefield() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent source = findPermanent(player1, "Mutagen Man, Living Ooze");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MutagenManLivingOoze());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mutagen);
        assertThat(mutagen.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mutagen);
    }

    @Test
    void cannotActivateMutagenDuringOpponentsMainPhase() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent source = findPermanent(player1, "Mutagen Man, Living Ooze");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mutagen);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateTappedMutagen() {
        castMutagenMan(1);
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent source = findPermanent(player1, "Mutagen Man, Living Ooze");
        mutagen.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mutagen);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mutagenCannotTargetANoncreatureMutagen() {
        castMutagenMan(2);
        List<Permanent> mutagens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagens.getFirst()),
                0, mutagens.getLast().getId()))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(mutagens);
        assertThat(mutagens.getFirst().isTapped()).isFalse();
    }

    private void castMutagenMan(int xValue) {
        harness.setHand(player1, List.of(new MutagenManLivingOoze()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castArtifact(player1, 0, xValue);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
