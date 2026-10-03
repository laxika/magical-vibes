package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BristlepackSentry;
import com.github.laxika.magicalvibes.cards.b.BristlyBillSpineSower;
import com.github.laxika.magicalvibes.cards.f.FreestriderCommando;
import com.github.laxika.magicalvibes.cards.j.JaceReawakened;
import com.github.laxika.magicalvibes.cards.p.PrairieDog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalamityGallopingInferno.class, BristlepackSentry.class, BristlyBillSpineSower.class,
        PrairieDog.class, JaceReawakened.class, FreestriderCommando.class})
class CalamityGallopingInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Saddled attack creates two tapped and attacking copies and sacrifices them at the next end step")
    void saddledAttackCreatesAndSacrificesTwoCopies() {
        Permanent calamity = addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent saddler = addCreatureReady(player1, new BristlepackSentry());
        addCreatureReady(player2, new BristlepackSentry());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(calamity.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.validIds()).containsExactly(saddler.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(tokens);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(calamity, saddler);
    }

    @Test
    @DisplayName("A legendary saddler cannot be chosen for the copies")
    void legendarySaddlerIsNotEligible() {
        Permanent calamity = addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent legendarySaddler = addCreatureReady(player1, new BristlyBillSpineSower());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(calamity.isSaddled()).isTrue();
        assertThat(legendarySaddler.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Attacking without saddling creates no copies")
    void unsaddledAttackCreatesNoCopies() {
        addCreatureReady(player1, new CalamityGallopingInferno());
        addCreatureReady(player1, new BristlepackSentry());
        addCreatureReady(player2, new BristlepackSentry());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The repeated process can copy a different creature from a second saddle activation")
    void canChooseDifferentSaddlers() {
        addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent first = addCreatureReady(player1, new BristlepackSentry());
        Permanent second = addCreatureReady(player1, new PrairieDog());
        addCreatureReady(player2, new BristlepackSentry());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(permanent -> permanent.getCard().getName()))
                .containsExactlyInAnyOrder("Bristlepack Sentry", "Prairie Dog");
    }

    @Test
    @DisplayName("Choosing a saddler is mandatory when an eligible creature exists")
    void cannotDeclineCopyChoice() {
        addCreatureReady(player1, new CalamityGallopingInferno());
        addCreatureReady(player1, new BristlepackSentry());
        addCreatureReady(player2, new BristlepackSentry());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each token can attack a different defender from Calamity")
    void tokensCanChooseTheirOwnAttackTargets() {
        addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent saddler = addCreatureReady(player1, new BristlepackSentry());
        addCreatureReady(player2, new BristlepackSentry());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceReawakened());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));
        PendingInteraction.PermanentChoice attackChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(attackChoice).isNotNull();
        assertThat(attackChoice.validIds()).contains(jace.getId(), player2.getId());
        harness.handlePermanentChosen(player1, jace.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(Permanent::getAttackTarget))
                .containsExactlyInAnyOrder(jace.getId(), player2.getId());
    }

    @Test
    @DisplayName("Copies apply their own enter-with-counters ability without copying the saddler's counters")
    void copiesApplyTheirOwnEntryAbility() {
        addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent saddler = harness.enterBattlefieldAndReturn(player1, new FreestriderCommando());
        saddler.setSummoningSick(true);
        saddler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        addCreatureReady(player2, new BristlepackSentry());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(saddler.isTapped()).isTrue();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("Saddle cannot be activated during combat")
    void saddleRequiresSorceryTiming() {
        addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent saddler = addCreatureReady(player1, new BristlepackSentry());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(saddler.isTapped()).isFalse();
    }
}
