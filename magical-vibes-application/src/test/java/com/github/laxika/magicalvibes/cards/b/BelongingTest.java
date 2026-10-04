package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Belonging.class, RayOfCommand.class})
class BelongingTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates three changeling Shapeshifter tokens")
    void entersAndCreatesShapeshifters() {
        harness.castFromHand(player1, new Belonging(), "{5}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
            assertThat(token.getCard().getKeywords()).containsExactly(Keyword.CHANGELING);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Encore creates an untapped hasty copy and sacrifices it at the next end step")
    void encoreCreatesUntappedTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Belonging()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Belonging");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Belonging")).isEmpty();
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3);
    }

    @Test
    @DisplayName("Encore exiles the source as an activation cost before creating any tokens")
    void encoreExilesSourceBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Belonging source = new Belonging();
        harness.setGraveyard(player1, List.of(source));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Belonging");
        assertThat(gd.findExiledCard(source.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Belonging");
        assertThat(findPermanents(player1, "Shapeshifter")).isEmpty();

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Belonging");
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3);
    }

    @Test
    @DisplayName("Encore cannot be activated during combat")
    void encoreRequiresSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new Belonging()));
        addManaForEncore();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Belonging");
        harness.assertNotOnBattlefield(player1, "Belonging");
    }

    @Test
    @DisplayName("An able Encore copy must be declared as an attacker")
    void encoreCopyCannotStayOutOfCombatWhenAbleToAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Belonging()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Belonging");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Encore copy stolen by an opponent survives the next end step")
    @CardUsed({Belonging.class, RayOfCommand.class})
    void stolenEncoreCopyIsNotSacrificed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Belonging()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Belonging");

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, token.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Belonging");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            resolveAllTriggers();
        });

        assertThat(findPermanent(player2, "Belonging").getId()).isEqualTo(token.getId());
        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(3);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
