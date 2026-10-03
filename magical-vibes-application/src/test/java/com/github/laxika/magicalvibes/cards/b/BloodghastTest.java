package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bloodghast.class, Forest.class, Disfigure.class})
class BloodghastTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent bloodghast = addCreatureReady(player1, new Bloodghast());

        assertThat(bls.canBlock(gd, bloodghast)).isFalse();
    }

    @Test
    @DisplayName("Has haste while an opponent has 10 or less life")
    void conditionalHaste() {
        Permanent bloodghast = harness.addToBattlefieldAndReturn(player1, new Bloodghast());

        assertThat(gqs.hasKeyword(gd, bloodghast, Keyword.HASTE)).isFalse();

        gd.playerLifeTotals.put(player2.getId(), 10);
        assertThat(gqs.hasKeyword(gd, bloodghast, Keyword.HASTE)).isTrue();

        gd.playerLifeTotals.put(player2.getId(), 11);
        assertThat(gqs.hasKeyword(gd, bloodghast, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("May return itself from the graveyard when a land enters")
    void landfallReturnsFromGraveyard() {
        Bloodghast bloodghast = new Bloodghast();
        harness.setGraveyard(player1, List.of(bloodghast));
        harness.setHand(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(bloodghast.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bloodghast.getId()));
    }

    @Test
    @DisplayName("Declining landfall keeps Bloodghast in the graveyard")
    void decliningLandfallKeepsItInGraveyard() {
        Bloodghast bloodghast = new Bloodghast();
        harness.setGraveyard(player1, List.of(bloodghast));
        harness.setHand(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(bloodghast.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bloodghast.getId()));
    }

    @Test
    @DisplayName("An opponent's land does not trigger the graveyard ability")
    void opponentLandDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new Bloodghast()));
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Low controller life does not grant haste while the opponent is above ten")
    void controllersLifeDoesNotGrantHaste() {
        Permanent bloodghast = harness.addToBattlefieldAndReturn(player1, new Bloodghast());
        harness.setLife(player1, 5);
        harness.setLife(player2, 11);

        assertThat(gqs.hasKeyword(gd, bloodghast, Keyword.HASTE)).isFalse();
        harness.setLife(player2, 9);
        assertThat(gqs.hasKeyword(gd, bloodghast, Keyword.HASTE)).isTrue();
        assertThat(bls.canBlock(gd, bloodghast)).isFalse();
    }

    @Test
    @DisplayName("Each graveyard copy has an independent optional return")
    void graveyardCopiesReturnIndependently() {
        Bloodghast first = new Bloodghast();
        Bloodghast second = new Bloodghast();
        harness.setGraveyard(player1, List.of(first, second));

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Bloodghast")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Bloodghast")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A pending landfall ability cannot return Bloodghast after it returns and dies again")
    void oldLandfallDoesNotReturnNewGraveyardObject() {
        harness.setGraveyard(player1, List.of(new Bloodghast()));
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Bloodghast");

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Bloodghast").getId());
        harness.assertInGraveyard(player1, "Bloodghast");
        harness.assertNotOnBattlefield(player1, "Bloodghast");

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Bloodghast");
        harness.assertNotOnBattlefield(player1, "Bloodghast");
        assertThat(gd.stack).isEmpty();
    }
}
