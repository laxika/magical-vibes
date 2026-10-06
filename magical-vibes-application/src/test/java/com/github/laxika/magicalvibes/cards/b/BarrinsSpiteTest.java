package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.e.EmpressGalina;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrinsSpite.class, BenalishLancer.class, CaptainSisay.class, EmpressGalina.class,
        KavuAggressor.class})
class BarrinsSpiteTest extends BaseCardTest {

    private void castBarrinsSpite(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new BarrinsSpite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
    }

    @Test
    @DisplayName("The targeted creatures' controller chooses which one to sacrifice")
    void controllerChoosesWhichCreatureToSacrifice() {
        Permanent kavu = addCreatureReady(player2, new KavuAggressor());
        Permanent lancer = addCreatureReady(player2, new BenalishLancer());

        castBarrinsSpite(kavu, lancer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(kavu.getId(), lancer.getId());
    }

    @Test
    @DisplayName("The chosen creature is sacrificed and the other returns to its owner's hand")
    void sacrificesChosenAndReturnsOther() {
        Permanent kavu = addCreatureReady(player2, new KavuAggressor());
        Permanent lancer = addCreatureReady(player2, new BenalishLancer());

        castBarrinsSpite(kavu, lancer);
        harness.handlePermanentChosen(player2, kavu.getId());

        harness.assertInGraveyard(player2, "Kavu Aggressor");
        harness.assertNotOnBattlefield(player2, "Kavu Aggressor");
        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInHand(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("With only one target left legal, that one is sacrificed and nothing returns")
    void singleRemainingTargetIsSacrificedWithoutReturningAnything() {
        Permanent kavu = addCreatureReady(player2, new KavuAggressor());
        Permanent lancer = addCreatureReady(player2, new BenalishLancer());

        harness.setHand(player1, List.of(new BarrinsSpite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(kavu.getId(), lancer.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(lancer);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Kavu Aggressor");
        harness.assertNotInHand(player2, "Kavu Aggressor");
        harness.assertNotInHand(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("The two targets must be controlled by the same player")
    void cannotTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = addCreatureReady(player1, new KavuAggressor());
        Permanent theirs = addCreatureReady(player2, new BenalishLancer());

        harness.setHand(player1, List.of(new BarrinsSpite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither target is affected once they no longer share a controller")
    void targetsWithDifferentControllersOnResolutionAreBothIllegal() {
        addCreatureReady(player1, new EmpressGalina());
        Permanent sisay = addCreatureReady(player2, new CaptainSisay());
        Permanent kavu = addCreatureReady(player2, new KavuAggressor());

        harness.setHand(player1, List.of(new BarrinsSpite()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(sisay.getId(), kavu.getId()));
        harness.activateAbility(player1, 0, 0, null, sisay.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Captain Sisay");
        harness.assertOnBattlefield(player2, "Kavu Aggressor");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Barrin's Spite");
        harness.assertOnBattlefield(player1, "Captain Sisay");
        harness.assertOnBattlefield(player2, "Kavu Aggressor");
        harness.assertNotInHand(player2, "Captain Sisay");
    }

    @Test
    @DisplayName("The controller may sacrifice the second target instead of the first")
    void canSacrificeSecondTarget() {
        Permanent kavu = addCreatureReady(player2, new KavuAggressor());
        Permanent lancer = addCreatureReady(player2, new BenalishLancer());

        castBarrinsSpite(kavu, lancer);
        harness.handlePermanentChosen(player2, lancer.getId());

        harness.assertInGraveyard(player2, "Benalish Lancer");
        harness.assertInHand(player2, "Kavu Aggressor");
        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertNotOnBattlefield(player2, "Kavu Aggressor");
    }

    @Test
    @DisplayName("A stolen creature returns to its owner's hand rather than its controller's")
    void returnsStolenCreatureToOwnersHand() {
        addCreatureReady(player1, new EmpressGalina());
        Permanent sisay = addCreatureReady(player2, new CaptainSisay());
        Permanent kavu = addCreatureReady(player1, new KavuAggressor());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, sisay.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Captain Sisay");

        castBarrinsSpite(kavu, sisay);
        harness.handlePermanentChosen(player1, kavu.getId());

        harness.assertInGraveyard(player1, "Kavu Aggressor");
        harness.assertInHand(player2, "Captain Sisay");
        harness.assertNotInHand(player1, "Captain Sisay");
        harness.assertNotOnBattlefield(player1, "Captain Sisay");
    }

    @Test
    @DisplayName("The spell has no effect when both targets have left the battlefield")
    void doesNothingWhenBothTargetsAreGone() {
        Permanent kavu = addCreatureReady(player2, new KavuAggressor());
        Permanent lancer = addCreatureReady(player2, new BenalishLancer());
        harness.setHand(player1, List.of(new BarrinsSpite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of(kavu.getId(), lancer.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(kavu, lancer));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Barrin's Spite");
        harness.assertNotInGraveyard(player2, "Kavu Aggressor");
        harness.assertNotInGraveyard(player2, "Benalish Lancer");
        harness.assertNotInHand(player2, "Kavu Aggressor");
        harness.assertNotInHand(player2, "Benalish Lancer");
    }
}
