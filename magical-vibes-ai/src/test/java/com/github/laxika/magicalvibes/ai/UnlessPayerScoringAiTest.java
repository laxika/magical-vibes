package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.c.Cleansing;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.cards.p.PlungeIntoDarkness;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TaintedSpecter;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.cards.w.WandOfIth;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** How the hard AI scores a payment it is asked to make: the branches as the payer sees them. */
@DisplayName("Hard AI scoring of the payments it is asked to make")
class UnlessPayerScoringAiTest extends HardAiDecisionEngineTestSupport {

    @Test
    @DisplayName("Mana Leak on its own spell: the hard AI pays {3} so its spell isn't countered")
    void paysToKeepItsSpellFromBeingCountered() {
        HardAiDecisionEngine ai = createHardAi(player1);
        harness.setHand(player1, List.of());
        RuneclawBear bear = new RuneclawBear();
        harness.castFromHand(player1, bear, "{1}{G}");
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Runeclaw Bear"));
    }

    @Test
    @DisplayName("Tainted Specter: the hard AI puts a card on top of its library rather than discard it")
    void scoresTheDiscardAimedAtItAsItsOwnLoss() {
        HardAiDecisionEngine ai = createHardAi(player2);
        Permanent specter = new Permanent(new TaintedSpecter());
        specter.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(specter);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new NobleElephant(), new NobleElephant()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        gd.status = GameStatus.RUNNING;
        for (int answers = 0; answers < 3 && gd.interaction.activeInteraction() != null; answers++) {
            ai.handleEvent(AiDecisionKind.INTERACTION);
        }

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Noble Elephant"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Noble Elephant");
    }

    @Test
    @DisplayName("Wand of Ith: the hard AI pays 1 life rather than discard its revealed land")
    void paysLifeToKeepTheRevealedCard() {
        HardAiDecisionEngine ai = createHardAi(player2);
        Permanent wand = new Permanent(new WandOfIth());
        gd.playerBattlefields.get(player1.getId()).add(wand);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cleansing: the hard AI pays 1 life for its own land")
    void paysForTheObjectItIsAskedAbout() {
        HardAiDecisionEngine ai = createHardAi(player2);
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new Cleansing(), "{W}{W}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cleansing: the hard AI doesn't pay life to keep an opponent's land")
    void doesNotPayForAnOpponentsObject() {
        HardAiDecisionEngine ai = createHardAi(player2);
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new Cleansing(), "{W}{W}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A cascade hit taxed by Thalia: the hard AI pays the cost increase and casts it")
    void paysAFreeCastsCostIncrease() {
        HardAiDecisionEngine ai = createHardAi(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        harness.addToBattlefieldAndReturn(player1, new Mountain());
        Divination hit = new Divination();
        harness.setLibrary(player1, List.of(hit));
        harness.castFromHand(player1, new BloodbraidElf(), "{2}{R}{G}");
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit);
    }

    @Test
    @DisplayName("Plunge into Darkness: the hard AI pays at most half its life, not all of it")
    void doesNotPayItsWholeLife() {
        HardAiDecisionEngine ai = createHardAi(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, inertLibrary());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new PlungeIntoDarkness()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of());
        harness.passBothPriorities();
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player1.getId());

        ai.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gd.getLife(player1.getId())).isBetween(10, 19);
    }
}
