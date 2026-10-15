package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TuktukScrapper.class, Ornithopter.class, DarksteelCitadel.class, GrizzlyBears.class,
        WalkingAtlas.class, LeylineOfTheVoid.class})
class TuktukScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Its Ally entry may destroy an artifact and damage its controller")
    void allyEntryDestroysArtifactAndDealsDamage() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castScrapper();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The artifact destruction may be declined")
    void destructionMayBeDeclined() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castScrapper();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No damage is dealt when the artifact is indestructible")
    void indestructibleArtifactIsNotDestroyedAndDealsNoDamage() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        castScrapper();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castScrapper();

        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
    }

    @Test
    @DisplayName("A non-Ally entry does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TuktukScrapper());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castScrapper() {
        harness.castFromHand(player1, new TuktukScrapper(), "{3}{R}");
    }

    @Test
    void exiledArtifactDoesNotCauseDamage() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        castScrapper();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Walking Atlas");
        harness.assertNotInGraveyard(player2, "Walking Atlas");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    void ownArtifactCanBeDestroyedAndDamagesItsController() {
        harness.addToBattlefield(player2, new TuktukScrapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        castScrapper();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Walking Atlas");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void entryWithoutArtifactsDoesNotLeaveAnInteraction() {
        castScrapper();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tuktuk Scrapper");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void anotherAllyEntryTriggersExistingScrapperAndCountsBothAllies() {
        harness.addToBattlefield(player1, new TuktukScrapper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        castScrapper();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Walking Atlas");
        harness.assertLife(player2, 18);
    }
}
