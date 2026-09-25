package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.r.RibbonsOfTheReikai;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoamDweller.class, KamiOfFalseHope.class, RibbonsOfTheReikai.class,
        TendoIceBridge.class, GoblinCohort.class})
class LoamDwellerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit allows putting a land from hand onto the battlefield tapped")
    void spiritSpellPutsLandTapped() {
        addLoamDweller();
        harness.setHand(player1, List.of(new KamiOfFalseHope(), new TendoIceBridge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, "Tendo Ice Bridge");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof TendoIceBridge);
    }

    @Test
    @DisplayName("Casting an Arcane spell allows putting a land from hand onto the battlefield tapped")
    void arcaneSpellPutsLandTapped() {
        addLoamDweller();
        harness.setHand(player1, List.of(new RibbonsOfTheReikai(), new TendoIceBridge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, "Tendo Ice Bridge");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-Spirit, non-Arcane spell does not trigger Loam Dweller")
    void unrelatedSpellDoesNotTrigger() {
        addLoamDweller();
        harness.setHand(player1, List.of(new GoblinCohort(), new TendoIceBridge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof TendoIceBridge);
    }

    @Test
    @DisplayName("Declining the may ability leaves the land in hand")
    void decliningMayLeavesLandInHand() {
        addLoamDweller();
        harness.setHand(player1, List.of(new KamiOfFalseHope(), new TendoIceBridge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof TendoIceBridge);
    }

    @Test
    @DisplayName("Accepting the trigger without a land in hand does nothing")
    void acceptingWithoutLandDoesNothing() {
        addLoamDweller();
        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof TendoIceBridge);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger Loam Dweller")
    void opponentCastingSpiritDoesNotTrigger() {
        addLoamDweller();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KamiOfFalseHope()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof TendoIceBridge);
    }

    private void addLoamDweller() {
        addCreatureReady(player1, new LoamDweller());
    }
}
