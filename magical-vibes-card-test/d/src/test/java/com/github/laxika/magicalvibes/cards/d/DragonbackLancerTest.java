package com.github.laxika.magicalvibes.cards.d;

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

@DisplayName("Dragonback Lancer")
@CardUsed({DragonbackLancer.class})
class DragonbackLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token")
    void attackingCreatesWarriorToken() {
        addCreatureReady(player1, new DragonbackLancer());

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
        });
    }

    @Test
    @DisplayName("The attack token is sacrificed at the beginning of the next end step")
    void attackTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new DragonbackLancer());

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
    @DisplayName("Each attacking Lancer creates its own Warrior and both are sacrificed")
    void multipleLancersCreateAndSacrificeTheirOwnTokens() {
        addCreatureReady(player1, new DragonbackLancer());
        addCreatureReady(player1, new DragonbackLancer());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).hasSize(2);
        assertThat(findPermanents(player2, "Warrior")).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        assertThat(findPermanents(player1, "Dragonback Lancer")).hasSize(2);
    }

    @Test
    @DisplayName("Mobilize creates and sacrifices its token even after the Lancer leaves")
    void mobilizeResolvesAfterSourceLeavesBattlefield() {
        Permanent lancer = addCreatureReady(player1, new DragonbackLancer());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(lancer);
        gd.playerGraveyards.get(player1.getId()).add(lancer.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).hasSize(1);
        assertThat(findPermanents(player1, "Dragonback Lancer")).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }
}
