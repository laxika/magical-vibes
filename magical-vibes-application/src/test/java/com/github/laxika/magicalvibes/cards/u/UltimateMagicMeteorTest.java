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

    private void castMeteorFromHand() {
        harness.setHand(player1, List.of(new UltimateMagicMeteor()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
