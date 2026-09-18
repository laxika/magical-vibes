package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Disperse;
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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lich.class, Forest.class, GrizzlyBears.class, Shock.class, Disenchant.class, Disperse.class})
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
}
