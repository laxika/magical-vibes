package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeiAssassins.class, WeiInfantry.class, WeiEliteCompanions.class})
class WeiAssassinsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target opponent's only creature automatically")
    void etbDestroysOnlyCreatureAutomatically() {
        harness.addToBattlefield(player2, new WeiInfantry());

        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Wei Infantry");
        harness.assertInGraveyard(player2, "Wei Infantry");
    }

    @Test
    @DisplayName("ETB prompts target opponent to choose which creature to destroy")
    void etbPromptsOpponentToChoose() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        Permanent eliteCompanions = harness.addToBattlefieldAndReturn(player2, new WeiEliteCompanions());
        harness.addToBattlefield(player1, new WeiInfantry());

        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(infantry.getId(), eliteCompanions.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DestroyChosenCreature.class);

        harness.handlePermanentChosen(player2, infantry.getId());

        harness.assertOnBattlefield(player2, "Wei Elite Companions");
        harness.assertInGraveyard(player2, "Wei Infantry");
        harness.assertOnBattlefield(player1, "Wei Infantry");
    }

    @Test
    @DisplayName("ETB does nothing when target opponent controls no creatures")
    void etbDoesNothingWithNoCreatures() {
        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no creatures to destroy")).isTrue();
    }

    @Test
    @DisplayName("ETB does not destroy an indestructible creature")
    void etbDoesNotDestroyIndestructibleCreature() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        infantry.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player2, "Wei Infantry");
        harness.assertNotInGraveyard(player2, "Wei Infantry");
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new WeiAssassins()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castWeiAssassins(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new WeiAssassins()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
