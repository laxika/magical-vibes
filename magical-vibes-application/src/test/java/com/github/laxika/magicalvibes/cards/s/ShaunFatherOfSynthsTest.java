package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ShaunFatherOfSynths.class, KrenkoMobBoss.class, GrizzlyBears.class})
class ShaunFatherOfSynthsTest extends BaseCardTest {

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
