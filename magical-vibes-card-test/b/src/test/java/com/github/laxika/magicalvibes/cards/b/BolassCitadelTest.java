package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GodPharaohsStatue;
import com.github.laxika.magicalvibes.cards.s.SparkHarvest;
import com.github.laxika.magicalvibes.cards.s.SparkReaper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolassCitadel.class, Forest.class, GrizzlyBears.class, GodPharaohsStatue.class,
        SparkHarvest.class, SparkReaper.class})
class BolassCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("casts a nonland spell from the top of the library by paying its mana value in life")
    void castsSpellFromLibraryTopByPayingLife() {
        harness.addToBattlefield(player1, new BolassCitadel());
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("plays a land from the top of the library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new BolassCitadel());
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot cast the top spell when its mana value exceeds life")
    void cannotCastTopSpellWithoutEnoughLife() {
        harness.addToBattlefield(player1, new BolassCitadel());
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        gd.playerLifeTotals.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("life");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("sacrifices ten nonland permanents and makes each opponent lose 10 life")
    void sacrificesTenNonlandPermanents() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new BolassCitadel());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(10);
        assertThat(citadel.isTapped()).isTrue();
    }

    @Test
    void topCardIsVisibleOnlyToControllerEvenWithoutPriority() {
        harness.addToBattlefield(player1, new BolassCitadel());
        harness.setLibrary(player1, List.of(new SparkReaper()));
        harness.forceActivePlayer(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Spark Reaper"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Spark Reaper"));
    }

    @Test
    void cannotCastCreatureDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new BolassCitadel());
        SparkReaper reaper = new SparkReaper();
        harness.setLibrary(player1, List.of(reaper));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(reaper);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotPlayAnotherLandAfterUsingLandPlay() {
        harness.addToBattlefield(player1, new BolassCitadel());
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), secondLand));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void landsAndOpponentsPermanentsCannotPaySacrificeCost() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new BolassCitadel());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new SparkReaper());
        }
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SparkReaper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(citadel.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void mustPayManaTaxInAdditionToLife() {
        harness.addToBattlefield(player1, new BolassCitadel());
        harness.addToBattlefield(player2, new GodPharaohsStatue());
        harness.setLibrary(player1, List.of(new SparkReaper()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromLibraryTop(player1);

        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spark Reaper");
    }

    @Test
    void cannotCastTaxedSpellWithoutMana() {
        harness.addToBattlefield(player1, new BolassCitadel());
        harness.addToBattlefield(player2, new GodPharaohsStatue());
        SparkReaper reaper = new SparkReaper();
        harness.setLibrary(player1, List.of(reaper));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(reaper);
        harness.assertLife(player1, 20);
    }

    @Test
    void canPaySparkHarvestAdditionalManaCost() {
        harness.addToBattlefield(player1, new BolassCitadel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SparkReaper());
        harness.setLibrary(player1, List.of(new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveFromLibraryTop(player1, target.getId());

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertNotOnBattlefield(player2, "Spark Reaper");
        harness.assertInGraveyard(player2, "Spark Reaper");
        harness.assertInGraveyard(player1, "Spark Harvest");
    }
}
