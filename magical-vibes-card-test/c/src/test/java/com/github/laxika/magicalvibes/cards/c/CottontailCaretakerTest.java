package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CottontailCaretaker.class, GlorySeeker.class, GrizzlyBears.class})
class CottontailCaretakerTest extends BaseCardTest {

    @Test
    void perpetuallyGivesOffspringToChosenWhiteCreatureCardInHand() {
        harness.setHand(player1, List.of(new CottontailCaretaker(), new GrizzlyBears(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castKickedCreature(player1, 1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    void grantedOffspringIsOptional() {
        harness.setHand(player1, List.of(new CottontailCaretaker(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Glory Seeker");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void resolvesWithoutChoiceWhenThereIsNoWhiteCreatureInHand() {
        harness.setHand(player1, List.of(new CottontailCaretaker(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cottontail Caretaker");
        harness.assertInHand(player2, "Glory Seeker");
    }

    @Test
    void resolvesWithoutChoiceWhenHandIsEmpty() {
        harness.setHand(player1, List.of(new CottontailCaretaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cottontail Caretaker");
    }

    @Test
    void payingOneOffspringCostDoesNotPayAnotherGrantedInstance() {
        harness.setHand(player1, List.of(
                new CottontailCaretaker(), new CottontailCaretaker(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void payingOneRepeatedOffspringCostCreatesOneCopy() {
        grantThreeOffspringInstances();
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}"));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void payingTwoRepeatedOffspringCostsCreatesTwoCopies() {
        grantThreeOffspringInstances();
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}", "{1}"));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    private void grantThreeOffspringInstances() {
        harness.setHand(player1, List.of(new CottontailCaretaker(), new CottontailCaretaker(),
                new CottontailCaretaker(), new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        for (int i = 0; i < 3; i++) {
            harness.castCreature(player1, 0);
            resolveAllTriggers();
            harness.handleCardChosen(player1, 2 - i);
        }
    }
}
