package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Phelddagrif.class, Pillage.class})
class PhelddagrifTest extends BaseCardTest {

    private Permanent addPhelddagrif(ManaColor mana) {
        Permanent phelddagrif = addCreatureReady(player1, new Phelddagrif());
        harness.addMana(player1, mana, 1);
        return phelddagrif;
    }

    @Test
    @DisplayName("{G} grants trample and gives the opponent a 1/1 Hippo token")
    void greenAbilityGrantsTrampleAndGivesHippo() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        harness.assertNotOnBattlefield(player1, "Hippo");
        assertThat(findPermanents(player2, "Hippo"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HIPPO);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Trample from {G} wears off at end of turn")
    void trampleWearsOff() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("{W} grants flying and gives the opponent 2 life")
    void whiteAbilityGrantsFlyingAndGivesLife() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Flying from {W} wears off at end of turn")
    void flyingWearsOff() {
        Permanent phelddagrif = addPhelddagrif(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(phelddagrif.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("The green ability requires green mana")
    void greenAbilityRequiresGreenMana() {
        addCreatureReady(player1, new Phelddagrif());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{U} bounces Phelddagrif and offers the opponent a card")
    void blueAbilityBouncesAndOffersDraw() {
        addPhelddagrif(ManaColor.BLUE);
        harness.setLibrary(player2, List.of(new Pillage()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phelddagrif");
        harness.assertInHand(player1, "Phelddagrif");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The opponent may decline the {U} draw")
    void opponentMayDeclineDraw() {
        addPhelddagrif(ManaColor.BLUE);
        harness.setLibrary(player2, List.of(new Pillage()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }
}
