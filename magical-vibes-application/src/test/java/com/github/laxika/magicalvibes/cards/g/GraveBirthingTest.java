package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.StoneHavenMedic;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveBirthing.class, GiantMantis.class, StoneHavenMedic.class, SmiteTheMonstrous.class})
class GraveBirthingTest extends BaseCardTest {

    @Test
    void targetOpponentChoosesCardCreatesScionAndDraws() {
        Card bears = new GiantMantis();
        Card giant = new StoneHavenMedic();
        Card drawn = new GiantMantis();
        harness.setGraveyard(player2, List.of(bears, giant));
        harness.setLibrary(player1, List.of(drawn));
        castGraveBirthing(player2.getId());

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(giant);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertScion(player1);
    }

    @Test
    void emptyGraveyardStillCreatesScionAndDraws() {
        Card drawn = new GiantMantis();
        harness.setLibrary(player1, List.of(drawn));
        castGraveBirthing(player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertScion(player1);
    }

    @Test
    void scionCanBeSacrificedForColorlessMana() {
        harness.setGraveyard(player2, List.of(new StoneHavenMedic()));
        castGraveBirthing(player2.getId());

        Permanent scion = assertScion(player1);
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    void opponentCanChooseANoncreatureAndLaterEffectsWaitForThatChoice() {
        Card creature = new GiantMantis();
        Card instant = new SmiteTheMonstrous();
        Card drawn = new StoneHavenMedic();
        harness.setGraveyard(player2, List.of(creature, instant));
        harness.setLibrary(player1, List.of(drawn));
        castGraveBirthing(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);

        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertScion(player1);
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
    }

    @Test
    void singleGraveyardCardIsExiledAndOnlyTheCasterGetsTheTokenAndCard() {
        Card exiled = new SmiteTheMonstrous();
        Card drawn = new GiantMantis();
        harness.setGraveyard(player1, List.of(exiled));
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player2, List.of(new GraveBirthing()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertScion(player2);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new GraveBirthing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGraveBirthing(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GraveBirthing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }

    private Permanent assertScion(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent scion = findPermanents(owner, "Eldrazi Scion").getFirst();
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(scion.getCard().getColor()).isNull();
        assertThat(scion.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
        return scion;
    }
}
