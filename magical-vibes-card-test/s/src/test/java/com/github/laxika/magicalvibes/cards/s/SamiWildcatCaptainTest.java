package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.cards.l.Lithobraking;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiWildcatCaptain.class, GrizzlyBears.class, JhoirasFamiliar.class, SylvokLifestaff.class,
        SurveyMechan.class, Lithobraking.class})
class SamiWildcatCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Spells you cast cost one less for each artifact you control")
    void reducesSpellsByNumberOfArtifactsControlled() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction scales with additional artifacts")
    void reductionScalesWithArtifacts() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction does not affect an opponent's spells")
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceSpellsWithoutArtifacts() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.setHand(player1, List.of(new SurveyMechan()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCountOpponentsArtifacts() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player2, new SurveyMechan());
        harness.setHand(player1, List.of(new SurveyMechan()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reducesNoncreatureSpells() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.setHand(player1, List.of(new Lithobraking()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void excessReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SurveyMechan());
        }
        harness.setHand(player1, List.of(new Lithobraking()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void excessReductionAllowsGenericOnlySpellForFree() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SurveyMechan());
        }
        harness.setHand(player1, List.of(new SurveyMechan()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void newlyResolvedArtifactsIncreaseReductionForLaterSpells() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.setHand(player1, List.of(new SurveyMechan(), new SurveyMechan()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Survey Mechan")).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void combinesWithOtherCostReductions() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        harness.setHand(player1, List.of(new SurveyMechan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceItsOwnCostBeforeEnteringBattlefield() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SurveyMechan());
        }
        harness.setHand(player1, List.of(new SamiWildcatCaptain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attacksWithoutTappingAndDealsDamageTwice() {
        var captain = addCreatureReady(player1, new SamiWildcatCaptain());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(captain.isTapped()).isFalse();
        harness.assertLife(player2, 12);
    }
}
