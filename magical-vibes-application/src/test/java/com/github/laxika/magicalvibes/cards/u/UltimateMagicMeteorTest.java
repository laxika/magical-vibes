package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltimateMagicMeteor.class, ArcaneSignet.class, Forest.class, FugitiveWizard.class})
class UltimateMagicMeteorTest extends BaseCardTest {

    @Test
    void handCastDealsSevenDamageToEachCreatureOnly() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castMeteorFromHand();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    void foretoldCastLetsControllerChooseAnArtifactOrLandForEachOpponent() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new FugitiveWizard());

        UltimateMagicMeteor meteor = new UltimateMagicMeteor();
        harness.setHand(player1, List.of(meteor));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(meteor.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, meteor.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(signet.getId(), forest.getId());

        harness.handlePermanentChosen(player1, signet.getId());

        harness.assertInGraveyard(player2, "Arcane Signet");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    void handCastLeavesArtifactsLandsAndPlayersUnaffected() {
        harness.addToBattlefield(player1, new ArcaneSignet());
        harness.addToBattlefield(player2, new ArcaneSignet());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castMeteorFromHand();

        harness.assertOnBattlefield(player1, "Arcane Signet");
        harness.assertOnBattlefield(player2, "Arcane Signet");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ultimate Magic: Meteor");
    }

    @Test
    void foretoldCastDealsSevenDamageBeforeChoosingAnOpponentsLand() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.addToBattlefield(player1, new ArcaneSignet());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castMeteorFromForetell();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(signet.getId(), forest.getId());
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(7);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(7);
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");

        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Arcane Signet");
        harness.assertOnBattlefield(player1, "Arcane Signet");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ultimate Magic: Meteor");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void foretoldCastDestroysTheOnlyEligiblePermanentWithoutPrompting() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new ArcaneSignet());

        castMeteorFromForetell();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player1, "Arcane Signet");
        harness.assertInGraveyard(player1, "Ultimate Magic: Meteor");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void foretoldCastResolvesWhenOpponentHasNoArtifactOrLand() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player1, new Forest());

        castMeteorFromForetell();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Ultimate Magic: Meteor");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castMeteorFromForetell() {
        UltimateMagicMeteor meteor = new UltimateMagicMeteor();
        harness.setHand(player1, List.of(meteor));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, meteor.getId());
        harness.passBothPriorities();
    }

    private void castMeteorFromHand() {
        harness.setHand(player1, List.of(new UltimateMagicMeteor()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }
}
