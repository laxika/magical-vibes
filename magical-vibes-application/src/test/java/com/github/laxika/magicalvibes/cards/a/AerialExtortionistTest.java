package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialExtortionist.class, AncientGrudge.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class AerialExtortionistTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles up to one nonland permanent")
    void entersTheBattlefieldExilesUpToOneNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        castAerialExtortionist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(target.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, target.getOriginalCard().getId());
        resolveStack();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Its combat-damage ability exiles up to one target nonland permanent")
    void combatDamageExilesUpToOneNonlandPermanent() {
        Permanent aerial = addCreatureReady(player1, new AerialExtortionist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        aerial.setAttacking(true);
        aerial.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Draws when another player casts a spell from outside their hand")
    void drawsWhenOpponentCastsFromGraveyard() {
        harness.addToBattlefield(player1, new AerialExtortionist());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player2, 0, fountain.getId());
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Does not draw when another player casts a spell from their hand")
    void doesNotDrawWhenOpponentCastsFromHand() {
        harness.addToBattlefield(player1, new AerialExtortionist());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player2, 0, fountain.getId());
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    private void castAerialExtortionist() {
        harness.setHand(player1, List.of(new AerialExtortionist()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
