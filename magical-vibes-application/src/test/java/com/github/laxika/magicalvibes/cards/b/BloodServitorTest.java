package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodServitor.class, Abrade.class})
class BloodServitorTest extends BaseCardTest {

    @Test
    @DisplayName("When Blood Servitor enters, one Blood token is created")
    void etbCreatesOneBloodToken() {
        harness.setHand(player1, List.of(new BloodServitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bloods = findPermanents(player1, "Blood");
        assertThat(bloods).hasSize(1);
    }

    @Test
    @DisplayName("Blood can be used immediately, discarding and sacrificing as costs before drawing")
    void bloodActivationPaysCostsBeforeDrawing() {
        BloodServitor discarded = new BloodServitor();
        BloodServitor drawn = new BloodServitor();
        harness.setHand(player1, List.of(new BloodServitor(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = findPermanent(player1, "Blood");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertOnBattlefield(player1, "Blood Servitor");
    }

    @Test
    @DisplayName("Blood cannot be activated without a card to discard")
    void bloodRequiresDiscard() {
        harness.setHand(player1, List.of(new BloodServitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = findPermanent(player1, "Blood");
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Blood")).containsExactly(blood);
        assertThat(blood.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enters trigger creates Blood even if Blood Servitor leaves before it resolves")
    void triggerSurvivesSourceRemoval() {
        harness.setHand(player1, List.of(new BloodServitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Blood")).isEmpty();

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, harness.getPermanentId(player1, "Blood Servitor"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Blood Servitor");
        assertThat(findPermanents(player1, "Blood")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Blood is created for Blood Servitor's controller")
    void opponentGetsTheirBloodToken() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BloodServitor()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }
}
