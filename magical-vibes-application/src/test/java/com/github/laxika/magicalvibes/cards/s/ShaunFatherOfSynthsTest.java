package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaunFatherOfSynths.class, KrenkoMobBoss.class, GrizzlyBears.class, GarrukPrimalHunter.class})
class ShaunFatherOfSynthsTest extends BaseCardTest {

    @Test
    @DisplayName("Shaun can stay back and the controller can decline the copy")
    void canDeclineCopyWithoutShaunAttacking() {
        Permanent shaun = addCreatureReady(player1, new ShaunFatherOfSynths());
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player1, List.of(1));
            harness.handlePermanentChosen(player1, krenko.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shaun, krenko);
        assertThat(shaun.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The Synth copy can attack a different defender from the original")
    void choosesWhichDefenderTheCopyAttacks() {
        addCreatureReady(player1, new ShaunFatherOfSynths());
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        garruk.setCounterCount(CounterType.LOYALTY, 3);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player1, List.of(1));
            harness.handlePermanentChosen(player1, krenko.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).contains(player2.getId(), garruk.getId());
            harness.handlePermanentChosen(player1, garruk.getId());
        });

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(krenko.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(copy.getAttackTarget()).isEqualTo(garruk.getId());
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Whenever you attack, Shaun may copy another attacking legendary creature")
    void createsTappedAttackingSynthCopy() {
        addCreatureReady(player1, new ShaunFatherOfSynths());
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player1, List.of(0, 1));

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).containsExactly(krenko.getId());

            harness.handlePermanentChosen(player1, krenko.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, copy)).contains(CardSubtype.SYNTH);
        assertThat(gqs.hasEffectiveSupertype(gd, copy, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("Shaun cannot copy a target that leaves before the trigger resolves")
    void doesNotCopyRemovedAttacker() {
        Permanent shaun = addCreatureReady(player1, new ShaunFatherOfSynths());
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player1, List.of(1));
            harness.handlePermanentChosen(player1, krenko.getId());
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, krenko));
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        });

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shaun);
    }

    @Test
    @DisplayName("Shaun's attack trigger rejects nonlegendary attackers")
    void rejectsNonlegendaryAttacker() {
        Permanent shaun = addCreatureReady(player1, new ShaunFatherOfSynths());
        Permanent nonlegendary = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shaun, nonlegendary);
    }

    @Test
    @DisplayName("When Shaun leaves, it exiles controlled Synth tokens")
    void leavesBattlefieldExilesControlledSynthTokens() {
        Permanent shaun = addCreatureReady(player1, new ShaunFatherOfSynths());
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, krenko.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent synthToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, shaun));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(synthToken).contains(krenko);
    }

    @Test
    @DisplayName("The attack trigger cannot target Shaun itself")
    void cannotTargetShaunItself() {
        Permanent shaun = addCreatureReady(player1, new ShaunFatherOfSynths());
        addCreatureReady(player1, new KrenkoMobBoss());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, shaun.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
