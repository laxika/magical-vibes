package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TheOmenkeel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CosimaGodOfTheVoyageTest extends BaseCardTest {

    @Test
    void castingTheBackFaceCreatesACrewableVehicle() {
        harness.setHand(player1, List.of(new CosimaGodOfTheVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent omenkeel = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        crew.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(omenkeel), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, omenkeel)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void acceptingVoyageCountersThenDecliningReturnsCosimaAndDraws() {
        Permanent cosima = harness.addToBattlefieldAndReturn(player1, new CosimaGodOfTheVoyage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledVoyageCounters).containsEntry(cosima.getOriginalCard().getId(), 1);

        Card drawCard = new GrizzlyBears();
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(drawCard));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent returnedCosima = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(cosima.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedCosima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
        assertThat(gd.exiledVoyageCounters).doesNotContainKey(cosima.getOriginalCard().getId());
    }

    @Test
    void omenkeelExilesDamageCountAndKeepsLandPermissionAfterLeaving() {
        Permanent omenkeel = harness.addToBattlefieldAndReturn(player1, new TheOmenkeel());
        omenkeel.setSummoningSick(false);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        crew.setSummoningSick(false);
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland, new Forest()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(omenkeel), null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(omenkeel)));
        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(omenkeel);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(omenkeel.getId())).hasSize(3);

        gd.playerBattlefields.get(player1.getId()).remove(omenkeel);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, nonland.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }
}
