package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoffinPuppetsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing two lands returns Coffin Puppets from the graveyard during upkeep")
    void sacrificesTwoLandsAndReturnsDuringUpkeep() {
        CoffinPuppets puppets = new CoffinPuppets();
        harness.setGraveyard(player1, List.of(puppets));
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupUpkeep();

        harness.activateGraveyardAbility(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                swamp.getId(), firstForest.getId(), secondForest.getId());
        assertThat(choice.validIds()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, firstForest.getId());
        harness.handlePermanentChosen(player1, secondForest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(puppets.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(puppets.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(swamp, bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstForest, secondForest);
    }

    @Test
    @DisplayName("Coffin Puppets requires control of a Swamp")
    void requiresSwamp() {
        harness.setGraveyard(player1, List.of(new CoffinPuppets()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        setupUpkeep();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");
    }

    @Test
    @DisplayName("Coffin Puppets can only be activated during your upkeep")
    void onlyDuringYourUpkeep() {
        CoffinPuppets puppets = new CoffinPuppets();
        harness.setGraveyard(player1, List.of(puppets));
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Coffin Puppets cannot be activated without two lands to sacrifice")
    void requiresTwoLandsToSacrifice() {
        harness.setGraveyard(player1, List.of(new CoffinPuppets()));
        harness.addToBattlefield(player1, new Swamp());
        setupUpkeep();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
    }
}
