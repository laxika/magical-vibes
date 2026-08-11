package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArtisanOfFormsTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic lets Artisan of Forms become a copy of the chosen creature")
    void heroicCopiesChosenCreature() {
        harness.addToBattlefield(player1, new ArtisanOfForms());
        Permanent artisan = findPermanent(player1, "Artisan of Forms");
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artisan.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(artisan.getCard().getPower()).isEqualTo(2);
        assertThat(artisan.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copied creature retains Artisan of Forms' Heroic ability")
    void copyRetainsHeroicAbility() {
        harness.addToBattlefield(player1, new ArtisanOfForms());
        Permanent artisan = findPermanent(player1, "Artisan of Forms");
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        castTargetingArtisan(artisan, bears);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artisan.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(artisan.getCard().getPower()).isEqualTo(3);
        assertThat(artisan.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger Artisan of Forms")
    void spellTargetingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArtisanOfForms());
        Permanent artisan = findPermanent(player1, "Artisan of Forms");
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castTargetingArtisan(Permanent artisan, Permanent copyTarget) {
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
