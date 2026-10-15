package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SharuumTheHegemon.class, EtheriumSculptor.class, DruidOfTheAnima.class, ObeliskOfEsper.class})
class SharuumTheHegemonTest extends BaseCardTest {

    /** Casts Sharuum and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castSharuum() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SharuumTheHegemon(), "{3}{W}{U}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted artifact card from graveyard to the battlefield")
    void etbReturnsArtifactToBattlefield() {
        EtheriumSculptor sculptor = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(sculptor));

        castSharuum();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(sculptor.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Etherium Sculptor");
        harness.assertNotInGraveyard(player1, "Etherium Sculptor");
    }

    @Test
    @DisplayName("A non-artifact card in the graveyard is not a legal target")
    void nonArtifactNotTargetable() {
        harness.setGraveyard(player1, List.of(new DruidOfTheAnima()));

        castSharuum();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Druid of the Anima");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        EtheriumSculptor sculptor = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(sculptor));

        castSharuum();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(sculptor.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Etherium Sculptor");
        harness.assertNotOnBattlefield(player1, "Etherium Sculptor");
    }

    @Test
    @DisplayName("Empty graveyard leaves no targeted ability on the stack")
    void emptyGraveyardNoTrigger() {
        castSharuum();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A legal target must be chosen even when the return will be declined")
    void cannotDeclineTargetSelection() {
        harness.setGraveyard(player1, List.of(new EtheriumSculptor()));
        castSharuum();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Artifact targets exclude non-artifacts and the opponent's graveyard")
    void onlyOwnArtifactsAreOffered() {
        EtheriumSculptor ownArtifact = new EtheriumSculptor();
        DruidOfTheAnima nonArtifact = new DruidOfTheAnima();
        EtheriumSculptor opposingArtifact = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(ownArtifact, nonArtifact));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        castSharuum();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(ownArtifact);
    }

    @Test
    @DisplayName("Noncreature artifacts can be returned and enter untapped")
    void returnsNoncreatureArtifactUntapped() {
        ObeliskOfEsper obelisk = new ObeliskOfEsper();
        harness.setGraveyard(player1, List.of(obelisk));
        castSharuum();
        harness.handleMultipleCardsChosen(player1, List.of(obelisk.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Obelisk of Esper");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(obelisk.getId());
                    assertThat(permanent.isTapped()).isFalse();
                });
    }

    @Test
    @DisplayName("A target leaving the graveyard before resolution is not returned")
    void missingTargetIsNotReturned() {
        EtheriumSculptor sculptor = new EtheriumSculptor();
        harness.setGraveyard(player1, List.of(sculptor));
        castSharuum();
        harness.handleMultipleCardsChosen(player1, List.of(sculptor.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Etherium Sculptor");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entering Sharuum can target itself after dying to the legend rule")
    void enteringSharuumCanTargetItselfAfterLegendRule() {
        var original = harness.addToBattlefieldAndReturn(player1, new SharuumTheHegemon());
        SharuumTheHegemon entering = new SharuumTheHegemon();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, entering, "{3}{W}{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, original.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(entering);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).contains(entering);
        harness.handleMultipleCardsChosen(player1, List.of(entering.getId()));
        assertThat(gd.stack).hasSize(1);
    }
}
