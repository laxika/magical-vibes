package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuseumNightwatch.class, WrathOfGod.class, Shock.class})
class MuseumNightwatchTest extends BaseCardTest {

    @Test
    @DisplayName("When Museum Nightwatch dies, its controller creates a 2/2 white and blue Detective token")
    void deathTriggerCreatesDetectiveToken() {
        harness.addToBattlefield(player1, new MuseumNightwatch());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().getPower()).isEqualTo(2);
        assertThat(detective.getCard().getToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(detective.getCard().getSubtypes()).contains(CardSubtype.DETECTIVE);
        assertThat(detective.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Disguise casts Museum Nightwatch face down")
    void disguiseCastsFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MuseumNightwatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Museum Nightwatch").isFaceDown()).isTrue();
    }

    @Test
    void opponentTargetingDisguisedNightwatchTriggersWardOnlyOnce() {
        castDisguisedNightwatch();
        Permanent nightwatch = findPermanent(player1, "Museum Nightwatch");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, nightwatch.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Museum Nightwatch");
        assertThat(findPermanents(player1, "Detective")).isEmpty();
    }

    @Test
    void faceDownDeathDoesNotCreateDetective() {
        castDisguisedNightwatch();
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Museum Nightwatch");
        assertThat(findPermanents(player1, "Detective")).isEmpty();
        assertThat(findPermanents(player2, "Detective")).isEmpty();
    }

    @Test
    void turningFaceUpRestoresDeathTriggerAndRemovesWard() {
        castDisguisedNightwatch();
        Permanent nightwatch = findPermanent(player1, "Museum Nightwatch");
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nightwatch));

        assertThat(nightwatch.isFaceDown()).isFalse();
        assertThat(findPermanents(player1, "Detective")).isEmpty();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, nightwatch.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Museum Nightwatch");
        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player2, "Detective")).isEmpty();
    }

    private void castDisguisedNightwatch() {
        harness.setHand(player1, List.of(new MuseumNightwatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
    }
}
