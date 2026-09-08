package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castScrapper() {
        harness.setHand(player1, List.of(new TuktukScrapper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
