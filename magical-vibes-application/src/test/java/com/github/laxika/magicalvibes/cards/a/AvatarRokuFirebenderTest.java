package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarRokuFirebender.class, GrizzlyBears.class, Forest.class,
        AvatarAang.class, AangMasterOfElements.class})
class AvatarRokuFirebenderTest extends BaseCardTest {

    @Test
    void addsSixRedManaWhenYouAttackUntilEndOfCombat() {
        addCreatureReady(player1, new AvatarRokuFirebender());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void addsManaWhenAnOpponentAttacks() {
        addCreatureReady(player1, new AvatarRokuFirebender());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    @Test
    void boostsTargetCreatureUntilEndOfTurn() {
        addCreatureReady(player1, new AvatarRokuFirebender());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        addCreatureReady(player1, new AvatarRokuFirebender());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void addsManaOnlyOnceWhenMultipleCreaturesAttackWithoutRoku() {
        harness.addToBattlefield(player1, new AvatarRokuFirebender());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    @Test
    void addsNoManaWhenNoCreaturesAttack() {
        addCreatureReady(player1, new AvatarRokuFirebender());

        declareAttackers(List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void combatManaPaysForRepeatedBoostsWithoutReturningAsStepsEnd() {
        addCreatureReady(player1, new AvatarRokuFirebender());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.activateAbility(player1, 0, null, target.getId());
            harness.passBothPriorities();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
            assertThat(target.getEffectivePower()).isEqualTo(5);

            harness.activateAbility(player1, 0, null, target.getId());
            harness.passBothPriorities();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
            assertThat(target.getEffectivePower()).isEqualTo(8);
            assertThat(target.getEffectiveToughness()).isEqualTo(2);
        });

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void attackManaDoesNotCountAsFirebendingOrTriggerAang() {
        harness.addToBattlefield(player1, new AvatarRokuFirebender());
        harness.addToBattlefield(player1, new AvatarAang());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(2));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
