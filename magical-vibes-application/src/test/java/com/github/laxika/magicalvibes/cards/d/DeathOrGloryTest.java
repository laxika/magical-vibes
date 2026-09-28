package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.c.CrimsonAcolyte;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathOrGlory.class, ArdentSoldier.class, CrimsonAcolyte.class, DrakeSkullCameo.class})
class DeathOrGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Controller separates creature cards and opponent chooses the pile to exile")
    void opponentChoosesPileToExile() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        Card artifact = new DrakeSkullCameo();
        harness.setGraveyard(player1, List.of(soldier, acolyte, artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validCardIds()).containsExactlyInAnyOrder(soldier.getId(), acolyte.getId());

        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId()));

        PendingInteraction.MayAbilityChoice pileChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(pileChoice).isNotNull();
        assertThat(pileChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Crimson Acolyte");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(soldier);
        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Choosing the second pile to exile returns the first pile")
    void opponentChoosesSecondPileToExile() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        harness.setGraveyard(player1, List.of(soldier, acolyte));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(soldier.getId()));
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Ardent Soldier");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(acolyte);
    }

    @Test
    @DisplayName("An empty pile is legal and returning the other pile puts all creatures onto the battlefield")
    void emptyPileCanBeChosen() {
        Card soldier = new ArdentSoldier();
        Card acolyte = new CrimsonAcolyte();
        Card artifact = new DrakeSkullCameo();
        harness.setGraveyard(player1, List.of(soldier, acolyte, artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Ardent Soldier");
        harness.assertOnBattlefield(player1, "Crimson Acolyte");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("A graveyard with no creature cards does not create a pile choice")
    void noCreatureCardsDoNothing() {
        Card artifact = new DrakeSkullCameo();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new DeathOrGlory()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
    }
}
