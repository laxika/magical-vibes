package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FinalVengeance;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PopularEgotist.class, FinalVengeance.class, GloriousAnthem.class, GrizzlyBears.class})
class PopularEgotistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants indestructible, taps the Egotist, and drains a target opponent")
    void sacrificingAnotherCreatureGrantsIndestructibleAndDrainsOpponent() {
        Permanent egotist = addEgotistReady();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, egotist, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(egotist.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another enchantment can pay the ability's cost")
    void sacrificingAnotherEnchantmentCanPayTheCost() {
        Permanent egotist = addEgotistReady();
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, egotist, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(egotist.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("The ability cannot sacrifice Popular Egotist itself")
    void cannotSacrificeItself() {
        addEgotistReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent egotist = addEgotistReady();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, egotist, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, egotist, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent egotist = harness.addToBattlefieldAndReturn(player1, new PopularEgotist());
        egotist.setSummoningSick(true);
        egotist.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, egotist, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(egotist.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void triggersWhenItIsSacrificedToAnotherSpell() {
        Permanent egotist = addEgotistReady();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PopularEgotist());
        harness.setHand(player1, java.util.List.of(new FinalVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), egotist.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Popular Egotist");
        harness.assertNotOnBattlefield(player2, "Popular Egotist");
    }

    @Test
    void opponentsPermanentsCannotPayTheCost() {
        addEgotistReady();
        harness.addToBattlefield(player2, new PopularEgotist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addEgotistReady() {
        return addCreatureReady(player1, new PopularEgotist());
    }
}
