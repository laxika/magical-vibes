package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@CardUsed({DuskImp.class, FlameBurst.class, Lich.class, Forest.class, GrizzlyBears.class, Shock.class, Disenchant.class, Disperse.class})
class LichTest extends BaseCardTest {

    @Test
    void losesLifeAsItEntersWithoutUsingTheStack() {
        harness.setLife(player1, 20);
        castLich();

        harness.assertLife(player1, 0);
        harness.assertOnBattlefield(player1, "Lich");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void negativeLifeDoesNotBecomeZeroWhenAnotherLichEnters() {
        harness.addToBattlefield(player1, new Lich());
        harness.setLife(player1, -4);
        castLich();

        harness.assertLife(player1, -4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    void entryRespectsLifeTotalCannotChange() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 20);
        castLich();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifeLossWithoutDamageDoesNotRequireSacrifices() {
        castLich();
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "test"));
        harness.runStateBasedActions();

        harness.assertLife(player1, -3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void lifeGainDrawsCardsInsteadAndDoesNotAffectOpponent() {
        castLich();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotPreventLosingFromDrawingAnEmptyLibrary() {
        castLich();
        harness.setLibrary(player1, List.of());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void damageTriggersNontokenSacrificesAndStillLosesLife() {
        castLich();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        var tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        shockController();
        harness.assertLife(player1, -2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).contains(first.getId(), second.getId()).doesNotContain(token.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(first.getId(), token.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Lich");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token).doesNotContain(first, second);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void insufficientPermanentsAreSacrificedBeforeLosing() {
        harness.addToBattlefield(player1, new Lich());
        harness.setLife(player1, 20);
        var tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        shockController();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lich");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void combatDamageAlsoRequiresSacrifices() {
        castLich();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertLife(player1, -2);
        harness.assertOnBattlefield(player1, "Lich");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    void damageStillTriggersWhenLifeTotalCannotChange() {
        harness.addToBattlefield(player1, new Lich());
        harness.addToBattlefield(player1, new PlatinumEmperion());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());

        shockController();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertLife(player1, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void lichItselfCanBeChosenAndItsDeathTriggerThenLoses() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        shockController();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(lich.getId(), first.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void eachLichTriggersSeparatelyForDamage() {
        harness.addToBattlefield(player1, new Lich());
        harness.addToBattlefield(player1, new Lich());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new Forest());

        shockController();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(third.getId(), fourth.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void graveyardTriggerLosesEvenAtPositiveLife() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, lich.getId());
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void returningToHandDoesNotTriggerLossAtPositiveLife() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, lich.getId());

        harness.assertNotOnBattlefield(player1, "Lich");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void returningToHandAtZeroLifeRemovesProtectionFromLosing() {
        castLich();
        Permanent lich = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, lich.getId());

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed(PlatinumAngel.class)
    void cannotLoseEffectStopsInsufficientSacrificeLoss() {
        harness.addToBattlefield(player1, new Lich());
        var tokenAngel = new PlatinumAngel();
        tokenAngel.setToken(true);
        harness.addToBattlefield(player1, tokenAngel);
        harness.setLife(player1, 20);

        shockController();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lich");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void castLich() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Lich()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void shockController() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
    }
    @Test
    @DisplayName("Entering the battlefield loses the controller's life total")
    void enteringTheBattlefieldLosesLifeTotal() {
        harness.castFromHand(player1, new Lich(), "{B}{B}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Life gain is replaced by drawing cards")
    void lifeGainDrawsCards() {
        harness.addToBattlefield(player1, new Lich());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Damage requires sacrificing nontoken permanents and allows choosing which ones")
    void damageRequiresSacrificingNontokenPermanents() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        Permanent firstImp = addCreatureReady(player1, new DuskImp());
        Permanent secondImp = addCreatureReady(player1, new DuskImp());
        DuskImp tokenCard = new DuskImp();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);

        dealTwoDamageToPlayer1();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstImp.getId(), secondImp.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lich, token)
                .doesNotContain(firstImp, secondImp);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Insufficient nontoken permanents cause the controller to lose")
    void insufficientNontokenPermanentsCauseLoss() {
        harness.addToBattlefield(player1, new Lich());
        DuskImp tokenCard = new DuskImp();
        tokenCard.setToken(true);
        harness.addToBattlefield(player1, tokenCard);

        dealTwoDamageToPlayer1();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Putting Lich into a graveyard makes its controller lose")
    void puttingLichIntoGraveyardCausesLoss() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lich));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void dealTwoDamageToPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
    }
}
