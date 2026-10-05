package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaustEyesOfTheGlade.class, AinokSurvivalist.class})
class KaustEyesOfTheGladeTest extends BaseCardTest {

    @Test
    void turnsAFaceDownAttackingCreatureYouControlFaceUp() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent attacker = castFaceDownSurvivalist();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(indexOf(attacker)));
            harness.activateAbility(player1, indexOf(kaust), null, attacker.getId());
            harness.passBothPriorities();
        });

        assertThat(attacker.isFaceDown()).isFalse();
    }

    @Test
    void drawsOnlyForCreaturesTurnedFaceUpThisTurn() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent turnedUpAttacker = castFaceDownSurvivalist();
        Permanent faceDownAttacker = castFaceDownSurvivalist();
        harness.setLibrary(player1, List.of(new AinokSurvivalist()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(indexOf(turnedUpAttacker), indexOf(faceDownAttacker)));
            harness.activateAbility(player1, indexOf(kaust), null, turnedUpAttacker.getId());
            harness.passBothPriorities();
        });

        resolveCombat();
        resolveAllTriggers();

        assertThat(turnedUpAttacker.isFaceDown()).isFalse();
        assertThat(faceDownAttacker.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetANonAttackingCreature() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent target = castFaceDownSurvivalist();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(kaust), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAFaceUpAttackingCreature() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent target = addCreatureReady(player1, new AinokSurvivalist());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(indexOf(target)));
            assertThatThrownBy(() ->
                    harness.activateAbility(player1, indexOf(kaust), null, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    @Test
    void drawsForEachCreatureTurnedFaceUpByItsOwnMorphAction() {
        addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent first = castFaceDownSurvivalist();
        Permanent second = castFaceDownSurvivalist();
        harness.setLibrary(player1, List.of(new AinokSurvivalist(), new AinokSurvivalist()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(indexOf(first), indexOf(second)));
            harness.addMana(player1, ManaColor.GREEN, 4);
            harness.turnFaceUp(player1, indexOf(first));
            harness.turnFaceUp(player1, indexOf(second));
        });

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawForAFaceUpCreatureThatWasNeverTurnedFaceUp() {
        addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent attacker = addCreatureReady(player1, new AinokSurvivalist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AinokSurvivalist()));

        declareAttackers(List.of(indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetAnOpponentsFaceDownAttackingCreature() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent attacker = castFaceDownSurvivalist(player2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
            assertThatThrownBy(() ->
                    harness.activateAbility(player1, indexOf(kaust), null, attacker.getId()))
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    private Permanent castFaceDownSurvivalist() {
        return castFaceDownSurvivalist(player1);
    }

    private Permanent castFaceDownSurvivalist(Player player) {
        AinokSurvivalist survivalist = new AinokSurvivalist();
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(survivalist));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player.getId()).stream()
                .filter(Permanent::isFaceDown)
                .reduce((first, second) -> second)
                .orElseThrow();
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
