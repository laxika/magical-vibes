package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SabinMasterMonk.class, CommandTower.class})
class SabinMasterMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not use blitz")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new SabinMasterMonk()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sabin);
    }

    @Test
    @DisplayName("Blitz from hand grants haste, draws on death, and sacrifices at the next end step")
    void blitzFromHandGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new SabinMasterMonk(), new CommandTower()));
        harness.setLibrary(player1, List.of(new CommandTower()));
        addBlitzMana();

        harness.castInstantWithAlternateDiscards(player1, 0, null, 1, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sabin, Master Monk");
        harness.assertInHand(player1, "Command Tower");
    }

    @Test
    @DisplayName("Blitz can be cast from the graveyard by discarding a card")
    void blitzFromGraveyardGrantsHasteDrawsAndSacrifices() {
        harness.setGraveyard(player1, List.of(new SabinMasterMonk()));
        harness.setHand(player1, List.of(new CommandTower()));
        harness.setLibrary(player1, List.of(new CommandTower()));
        addBlitzMana();

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sabin, Master Monk");
        harness.assertInHand(player1, "Command Tower");
    }

    @Test
    @DisplayName("Blitz from hand grants haste immediately without an enters trigger")
    void blitzFromHandHasHasteAsItEnters() {
        harness.setHand(player1, List.of(new SabinMasterMonk(), new CommandTower()));
        addBlitzMana();

        harness.castInstantWithAlternateDiscards(player1, 0, null, 1, List.of());
        harness.assertInGraveyard(player1, "Command Tower");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blitz from graveyard grants haste immediately without an enters trigger")
    void blitzFromGraveyardHasHasteAsItEnters() {
        harness.setGraveyard(player1, List.of(new SabinMasterMonk()));
        harness.setHand(player1, List.of(new CommandTower()));
        addBlitzMana();

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of());
        harness.assertInGraveyard(player1, "Command Tower");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sabin cannot pay its own discard cost when blitzed from hand")
    void blitzFromHandRequiresAnotherCardToDiscard() {
        harness.setHand(player1, List.of(new SabinMasterMonk()));
        addBlitzMana();

        assertThatThrownBy(() -> harness.castInstantWithAlternateDiscards(
                player1, 0, null, 0, List.of())).isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Sabin, Master Monk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blitz permits attacking immediately and double strike deals damage twice")
    void blitzAttacksForEightDamage() {
        harness.setHand(player1, List.of(new SabinMasterMonk(), new CommandTower()));
        addBlitzMana();
        harness.castInstantWithAlternateDiscards(player1, 0, null, 1, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 12);
    }

    private void addBlitzMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
