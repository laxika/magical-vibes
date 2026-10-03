package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtisanOfForms.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class})
class ArtisanOfFormsTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic lets Artisan of Forms become a copy of the chosen creature")
    void heroicCopiesChosenCreature() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
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
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
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
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The controller may decline the copy after choosing its target")
    void mayDeclineCopy() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artisan.getCard().getName()).isEqualTo("Artisan of Forms");
    }

    @Test
    @DisplayName("Artisan can target itself with its copy ability")
    void heroicCanCopyItself() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, artisan.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(artisan.getId());
        harness.handlePermanentChosen(player1, artisan.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Artisan of Forms");
    }

    @Test
    @DisplayName("An older heroic trigger still changes the same permanent after a newer trigger copies it")
    void pendingCopyStillResolvesAfterAnotherCopy() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(artisan.getCard().getName()).isEqualTo("Hill Giant");

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artisan.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's spell targeting Artisan does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, artisan.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Copying does not copy temporary boosts and lasts beyond the turn")
    void copyIgnoresTemporaryBoostAndPersists() {
        Permanent artisan = harness.addToBattlefieldAndReturn(player1, new ArtisanOfForms());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(5);

        harness.castInstant(player1, 0, artisan.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameQueryService().getEffectivePower(gd, artisan)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(artisan.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, artisan)).isEqualTo(2);
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
