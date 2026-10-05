package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FireMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QutrubForayer.class, Forest.class, GrizzlyBears.class, FireMagic.class})
class QutrubForayerTest extends BaseCardTest {

    @Test
    void destroysCreatureThatWasDealtDamageThisTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        cast(0, targetId);
        resolveCreatureAndEtb();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canBeCastWithoutChoosingAModeOrTargetBeforeItEnters() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new QutrubForayer(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Exile up to two target cards from a single graveyard");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qutrub Forayer");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void destroyModeRejectsAnUndamagedCreatureWhenChoosingTheTriggeredAbilityTarget() {
        UUID damagedId = harness.addToBattlefieldAndReturn(player2, new QutrubForayer()).getId();
        UUID undamagedId = harness.addToBattlefieldAndReturn(player2, new QutrubForayer()).getId();
        gd.permanentsDealtDamageThisTurn.add(damagedId);

        assertThatThrownBy(() -> cast(0, undamagedId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    void exilesUpToTwoCardsFromOneGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card left = new Forest();
        harness.setGraveyard(player2, List.of(first, second, left));

        cast(1, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(left);
    }

    @Test
    void choosesExileModeWhenEnteringWithoutBeingCast() {
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));

        harness.enterBattlefieldAndReturn(player1, new QutrubForayer());
        harness.handleListChoice(player1, "Exile up to two target cards from a single graveyard");
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void mayChooseZeroCardsEvenWhenGraveyardsAreNotEmpty() {
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));

        cast(1, null);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exileModeResolvesWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        cast(1, null);
        resolveCreatureAndEtb();

        harness.assertOnBattlefield(player1, "Qutrub Forayer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canExileOneCardFromItsControllersGraveyard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));

        cast(1, null);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    void rejectsCardsFromDifferentGraveyards() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));

        cast(1, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
    }

    @Test
    void exilesRemainingLegalTargetWhenAnotherTargetLeavesTheGraveyard() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setGraveyard(player2, List.of(first, second));

        cast(1, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player2, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void destroysCreatureDealtNoncombatDamageThisTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new QutrubForayer()).getId();
        harness.setHand(player1, List.of(new FireMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        cast(0, targetId);
        resolveCreatureAndEtb();

        harness.assertInGraveyard(player2, "Qutrub Forayer");
    }

    private void cast(int mode, UUID targetId) {
        harness.castFromHand(player1, new QutrubForayer(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0
                ? "Destroy target creature that was dealt damage this turn"
                : "Exile up to two target cards from a single graveyard");
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
    }
}
