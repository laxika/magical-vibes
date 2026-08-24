package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoseiTheMorningStar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalamityGallopingInferno.class, GrizzlyBears.class, YoseiTheMorningStar.class})
class CalamityGallopingInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Saddled attack creates two tapped and attacking copies and sacrifices them at the next end step")
    void saddledAttackCreatesAndSacrificesTwoCopies() {
        Permanent calamity = addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(calamity.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();

        declareAttackers(List.of(0));
        for (int i = 0; i < 8 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }

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
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(tokens);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(calamity, saddler);
    }

    @Test
    @DisplayName("A legendary saddler cannot be chosen for the copies")
    void legendarySaddlerIsNotEligible() {
        Permanent calamity = addCreatureReady(player1, new CalamityGallopingInferno());
        Permanent legendarySaddler = addCreatureReady(player1, new YoseiTheMorningStar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(calamity.isSaddled()).isTrue();
        assertThat(legendarySaddler.isTapped()).isTrue();

        declareAttackers(List.of(0));
        for (int i = 0; i < 8 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
