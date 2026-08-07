package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DracoplasmTest extends BaseCardTest {

    private void castDracoplasm() {
        harness.setHand(player1, new ArrayList<>(List.of(new Dracoplasm())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent dracoplasm() {
        return findPermanent(player1, "Dracoplasm");
    }

    @Test
    @DisplayName("Power and toughness become the total power and toughness of the sacrificed creatures")
    void powerToughnessSumSacrificedCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());  // 2/2
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());  // 2/4

        castDracoplasm();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), spider.getId()));

        Permanent dracoplasm = dracoplasm();
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

        Permanent dracoplasm = dracoplasm();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, dracoplasm)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dracoplasm)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sacrificing nothing leaves a 0/0 that dies to state-based actions")
    void sacrificingNothingDiesAsZeroZero() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDracoplasm();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dracoplasm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dracoplasm"));
    }

    @Test
    @DisplayName("With no other creatures it enters as a 0/0 with no choice offered")
    void noOtherCreaturesNoChoice() {
        castDracoplasm();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dracoplasm"));
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
    @DisplayName("{R} pumps it +1/+0 on top of the P/T it entered with")
    void firebreathingStacksOnEnteredPowerToughness() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());  // 2/2

        castDracoplasm();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent dracoplasm = dracoplasm();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, dracoplasm)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dracoplasm)).isEqualTo(2);
    }
}
