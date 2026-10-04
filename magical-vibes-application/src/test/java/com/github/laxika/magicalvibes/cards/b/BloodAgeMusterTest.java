package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChargingStrifeknight;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.f.FumingEffigy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarruksUprising;
import com.github.laxika.magicalvibes.cards.p.PillardropWarden;
import com.github.laxika.magicalvibes.cards.s.SpiritMascot;
import com.github.laxika.magicalvibes.cards.s.StoneDocent;
import com.github.laxika.magicalvibes.cards.s.StonebindersFamiliar;
import com.github.laxika.magicalvibes.cards.s.StoneboundMentor;
import com.github.laxika.magicalvibes.cards.s.StoneriseSpirit;
import com.github.laxika.magicalvibes.cards.s.SummonedDromedary;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        BloodAgeMuster.class,
        BloodAgeGeneral.class,
        ChargingStrifeknight.class,
        FumingEffigy.class,
        PillardropWarden.class,
        SpiritMascot.class,
        StoneDocent.class,
        StonebindersFamiliar.class,
        StoneboundMentor.class,
        StoneriseSpirit.class,
        SummonedDromedary.class,
        Disentomb.class,
        GrizzlyBears.class
})
class BloodAgeMusterTest extends BaseCardTest {

    @Test
    void conjuresSpellbookCreatureWithPerpetualBaseStats() {
        Permanent muster = addMuster();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        List<Permanent> created = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(muster.getId()))
                .toList();
        assertThat(created).hasSize(1);
        assertThat(created.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(created.getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        Permanent muster = addMuster();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(muster.getId()))
                .findFirst()).isPresent();
    }

    @Test
    void doesNotTriggerForCardsLeavingOpponentsGraveyard() {
        addMuster();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new SummonedDromedary()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof SummonedDromedary);
    }

    @Test
    void eachMusterTriggersIndependently() {
        addMuster();
        addMuster();
        harness.setGraveyard(player1, List.of(new SummonedDromedary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void canTriggerAgainOnALaterTurn() {
        addMuster();
        harness.setGraveyard(player1, List.of(new SummonedDromedary()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SummonedDromedary()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        resolveAllTriggers();
        if (gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.Scry.class) != null) {
            gs.handleInteractionAnswer(gd, player1,
                    new com.github.laxika.magicalvibes.service.interaction.InteractionAnswer.ScryOrder(
                            List.of(0), List.of()));
            resolveAllTriggers();
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @CardUsed(GarruksUprising.class)
    void entryTriggersSeePowerBeforeTheSubsequentPerpetualChange() {
        Permanent muster = addMuster();
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new GarruksUprising());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BloodAgeGeneral()));
        harness.setGraveyard(player1, List.of(new SummonedDromedary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(muster.getId())
                        && !permanent.getId().equals(uprising.getId()))
                .findFirst().orElseThrow();
        boolean enteredWithPowerFour = List.of("Fuming Effigy", "Summoned Dromedary")
                .contains(conjured.getCard().getName());
        if (enteredWithPowerFour) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(enteredWithPowerFour ? 2 : 1);
        assertThat(conjured.getCard().isToken()).isFalse();
        assertThat(gqs.getEffectivePower(gd, conjured)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, conjured)).isEqualTo(2);
    }

    @Test
    void triggersForGraveyardExileCostsDuringOpponentsTurn() {
        addMuster();
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BloodAgeGeneral()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, spirit.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    private Permanent addMuster() {
        return harness.addToBattlefieldAndReturn(player1, new BloodAgeMuster());
    }
}
