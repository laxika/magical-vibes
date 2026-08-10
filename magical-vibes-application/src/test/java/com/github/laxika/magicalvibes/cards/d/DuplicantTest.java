package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicantTest extends BaseCardTest {

    private void castDuplicantAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
    }

    @Test
    @DisplayName("Exiles a nontoken creature and takes its power, toughness, and creature types")
    void copiesExiledCreatureCharacteristics() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castDuplicantAndAcceptMay(bearsId);

        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.SHAPESHIFTER);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the imprint keeps Duplicant's printed characteristics")
    void decliningMayKeepsPrintedCharacteristics() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent duplicant = findPermanent(player1, "Duplicant");
        assertThat(gqs.getEffectivePower(gd, duplicant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duplicant)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, duplicant))
                .containsExactly(CardSubtype.SHAPESHIFTER);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The imprint ability does not trigger when no nontoken creature is available")
    void noLegalTargetMeansNoTrigger() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Target selection excludes token creatures")
    void tokenIsNotALegalTarget() {
        harness.addToBattlefield(player2, tokenCreature());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Duplicant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Bear Token");
    }

    private com.github.laxika.magicalvibes.model.Card tokenCreature() {
        com.github.laxika.magicalvibes.model.Card token = new com.github.laxika.magicalvibes.model.Card();
        token.setName("Bear Token");
        token.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        token.setPower(2);
        token.setToughness(2);
        token.setSubtypes(List.of(CardSubtype.BEAR));
        token.setToken(true);
        return token;
    }
}
