package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Avenger of the Fallen")
@CardUsed({AvengerOfTheFallen.class, GrizzlyBears.class, LightningBolt.class})
class AvengerOfTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token for each creature card in the graveyard")
    void attackingCreatesWarriorTokensForCreatureCardsInGraveyard() {
        addCreatureReady(player1, new AvengerOfTheFallen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Attack tokens are sacrificed at the beginning of the next end step")
    void attackTokensAreSacrificedAtNextEndStep() {
        addCreatureReady(player1, new AvengerOfTheFallen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Mobilize ignores creature cards in the opponent's graveyard")
    void ignoresOpponentsGraveyard() {
        addCreatureReady(player1, new AvengerOfTheFallen());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Mobilize counts creature cards added before its ability resolves")
    void countsGraveyardAtResolution() {
        addCreatureReady(player1, new AvengerOfTheFallen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
    }

    @Test
    @DisplayName("Mobilize creates no tokens if the graveyard is emptied before resolution")
    void createsNoTokensWhenGraveyardEmptiedBeforeResolution() {
        addCreatureReady(player1, new AvengerOfTheFallen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }

    @Test
    @DisplayName("One mobilize resolution creates one delayed sacrifice trigger for all its tokens")
    void sacrificesAllTokensWithOneDelayedTrigger() {
        addCreatureReady(player1, new AvengerOfTheFallen());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }
}
