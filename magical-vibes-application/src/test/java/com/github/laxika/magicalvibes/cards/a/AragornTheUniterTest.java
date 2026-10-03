package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AragornTheUniter.class, ArwenMortalQueen.class, Divination.class, GiantGrowth.class, GrizzlyBears.class,
        SavannahLions.class, Shock.class})
class AragornTheUniterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a white spell creates a Human Soldier token")
    void whiteSpellCreatesHumanSoldier() {
        addReadyAragorn();
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a blue spell causes scry 2")
    void blueSpellScriesTwo() {
        addReadyAragorn();
        harness.setLibrary(player1, List.of(new Card[]{
                new GrizzlyBears(), new SavannahLions(), new Shock(), new GiantGrowth()
        }));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Card firstCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(firstCard);
    }

    @Test
    @DisplayName("Casting a red spell deals 3 damage to a target opponent")
    void redSpellDamagesTargetOpponent() {
        addReadyAragorn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Casting a green spell gives a target creature +4/+4 until end of turn")
    void greenSpellBoostsTargetCreature() {
        addReadyAragorn();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
    }

    @Test
    @DisplayName("A green-white spell triggers both abilities before entering the battlefield")
    void multicoloredSpellTriggersBothMatchingAbilities() {
        addReadyAragorn();
        Permanent aragorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ArwenMortalQueen()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, aragorn.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, aragorn)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, aragorn)).isEqualTo(9);
        harness.assertNotOnBattlefield(player1, "Arwen, Mortal Queen");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's red spell does not trigger Aragorn")
    void opponentSpellDoesNotTrigger() {
        addReadyAragorn();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The green trigger can boost an opponent's creature and expires at end of turn")
    void greenTriggerCanTargetOpponentCreatureAndExpires() {
        addReadyAragorn();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The red trigger resolves before the triggering spell")
    void redTriggerResolvesBeforeSpell() {
        addReadyAragorn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The controller chooses the order of simultaneous matching color triggers")
    void controllerChoosesOrderOfMatchingTriggers() {
        addReadyAragorn();
        Permanent aragorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ArwenMortalQueen()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, aragorn.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private void addReadyAragorn() {
        Permanent aragorn = harness.addToBattlefieldAndReturn(player1, new AragornTheUniter());
        aragorn.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
