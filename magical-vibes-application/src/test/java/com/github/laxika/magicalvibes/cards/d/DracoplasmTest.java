package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dracoplasm.class, GrizzlyBears.class, GiantSpider.class, HillGiant.class,
        GoblinKing.class, GoblinPiker.class})
class DracoplasmTest extends BaseCardTest {

    private void castDracoplasm() {
        harness.castFromHand(player1, new Dracoplasm(), "{U}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Power and toughness become the total power and toughness of the sacrificed creatures")
    void powerToughnessSumSacrificedCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());  // 2/2
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());  // 2/4

        castDracoplasm();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), spider.getId()));

        Permanent dracoplasm = findPermanent(player1, "Dracoplasm");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, dracoplasm)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dracoplasm)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Giant Spider");
    }

    @Test
    @DisplayName("Only the chosen creatures are sacrificed; the rest stay on the battlefield")
    void unchosenCreaturesSurvive() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());          // 2/2, kept
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant()); // 3/3

        castDracoplasm();
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

        Permanent dracoplasm = findPermanent(player1, "Dracoplasm");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, dracoplasm)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dracoplasm)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing nothing leaves a 0/0 that dies to state-based actions")
    void sacrificingNothingDiesAsZeroZero() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDracoplasm();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Dracoplasm");
        harness.assertInGraveyard(player1, "Dracoplasm");
    }

    @Test
    @DisplayName("With no other creatures it enters as a 0/0 with no choice offered")
    void noOtherCreaturesNoChoice() {
        castDracoplasm();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Dracoplasm");
    }

    @Test
    @DisplayName("Opponent creatures can't be sacrificed to it")
    void opponentCreaturesNotOffered() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castDracoplasm();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(mine.getId());
    }

    @Test
    @DisplayName("Uses the selected creatures' power and toughness before sacrificing them")
    void sumsSelectedCreaturesBeforeSacrificingThem() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new GoblinKing());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        castDracoplasm();
        harness.handleMultiplePermanentsChosen(player1, List.of(king.getId(), piker.getId()));

        Permanent dracoplasm = findPermanent(player1, "Dracoplasm");
        assertThat(gqs.getEffectivePower(gd, dracoplasm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dracoplasm)).isEqualTo(4);
    }

    @Test
    @DisplayName("{R} pumps it +1/+0 on top of the P/T it entered with")
    void firebreathingStacksOnEnteredPowerToughness() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());  // 2/2

        castDracoplasm();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent dracoplasm = findPermanent(player1, "Dracoplasm");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, dracoplasm)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dracoplasm)).isEqualTo(2);
    }
}
