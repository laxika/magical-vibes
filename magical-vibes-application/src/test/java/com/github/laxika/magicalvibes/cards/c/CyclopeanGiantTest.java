package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshcoatBear.class, CyclopeanGiant.class, Forest.class, SuddenShock.class, TerramorphicExpanse.class})
class CyclopeanGiantTest extends BaseCardTest {

    @Test
    void deathTriggerTurnsTargetLandIntoSwampAndExilesGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CyclopeanGiant());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID giantId = giant.getId();
        UUID forestId = forest.getId();
        UUID giantCardId = giant.getCard().getId();

        harness.castAndResolveInstant(player2, 0, giantId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giantCardId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(giantCardId));
    }

    @Test
    void deathTriggerOnlyOffersLandsAsTargets() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CyclopeanGiant());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new AshcoatBear());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, giant.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
    }

    @Test
    void deathTriggerReplacesNonbasicLandsAbilitiesAndAddsSwampMana() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CyclopeanGiant());
        Permanent expanse = harness.addToBattlefieldAndReturn(player2, new TerramorphicExpanse());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, giant.getId());

        harness.handlePermanentChosen(player1, expanse.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, expanse)).containsExactly(CardSubtype.SWAMP);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");

        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
