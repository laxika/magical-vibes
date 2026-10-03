package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.m.MoonrageBrute;
import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalCathar.class, MoonrageBrute.class, CandlegroveWitch.class, PlayWithFire.class})
class BrutalCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling a creature an opponent controls")
    void etbExilesOpponentCreatureUntilCatharLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        Permanent cathar = castCathar(bears.getId());

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears.getCard());

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, cathar.getId());

        harness.assertOnBattlefield(player2, "Candlegrove Witch");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Transforms into Moonrage Brute when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player1);

        assertThat(cathar.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(cathar.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms back and exiles a new opposing creature")
    void transformsBackAndExilesCreature() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(cathar.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
    }

    @Test
    void startsDayWhenEnteringBeforeDayOrNightHasBeenEstablished() {
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(findPermanent(player1, "Brutal Cathar").isTransformed()).isFalse();
    }

    @Test
    void entersAtNightWithoutExilingAnOpponentCreature() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Moonrage Brute").isTransformed()).isTrue();
        harness.assertOnBattlefield(player2, "Candlegrove Witch");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsInstantDuringPreviousTurnDoesNotPreventNight() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(cathar.isTransformed()).isTrue();
    }

    @Test
    void twoInstantsByNonactivePlayerDoNotMakeItDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent cathar = harness.enterBattlefieldAndReturn(player1, new BrutalCathar());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(cathar.isTransformed()).isTrue();
    }

    @Test
    void wardCountersOpponentSpellWhenLifePaymentIsDeclined() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brute = harness.enterBattlefieldAndReturn(player1, new BrutalCathar());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, brute.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Play with Fire");
        harness.assertLife(player2, 20);
        assertThat(brute.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nightboundDoesNotCreateAnUpkeepTransformationTrigger() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brute = harness.enterBattlefieldAndReturn(player1, new BrutalCathar());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(brute.isTransformed()).isTrue();
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWardLifeLetsOpponentSpellResolve() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brute = harness.enterBattlefieldAndReturn(player1, new BrutalCathar());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, brute.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(brute.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void sourceLeavingBeforeExileResolvesDoesNotExileCreature() {
        Permanent witch = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);
        harness.castCreature(player1, 0, witch.getId());
        harness.passBothPriorities();
        Permanent cathar = findPermanent(player1, "Brutal Cathar");
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, cathar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brutal Cathar");
        harness.assertOnBattlefield(player2, "Candlegrove Witch");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void transformationDoesNotReturnExiledCreature() {
        Permanent witch = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        Permanent cathar = castCathar(witch.getId());
        gd.spellsCastLastTurn.clear();

        harness.performUntapStep(player2);

        assertThat(cathar.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(witch.getCard());
    }

    @Test
    void moonrageBruteDealsFirstStrikeDamageBeforeItsBlocker() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brute = harness.enterBattlefieldAndReturn(player1, new BrutalCathar());
        brute.setSummoningSick(false);
        harness.addToBattlefield(player2, new CandlegroveWitch());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Candlegrove Witch");
        harness.assertOnBattlefield(player1, "Moonrage Brute");
        assertThat(brute.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void leavingAsMoonrageBruteReturnsExiledCreatureWithoutWardForOwnSpells() {
        Permanent witch = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        Permanent cathar = castCathar(witch.getId());
        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player2);
        assertThat(cathar.isTransformed()).isTrue();
        resetForFollowUpSpell();
        harness.setHand(player1, List.of(new PlayWithFire(), new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, cathar.getId());
        harness.castAndResolveInstant(player1, 0, cathar.getId());

        harness.assertNotOnBattlefield(player1, "Moonrage Brute");
        harness.assertInGraveyard(player1, "Brutal Cathar");
        harness.assertOnBattlefield(player2, "Candlegrove Witch");
        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addCathar() {
        gd.dayNight = DayNight.DAY;
        return harness.addToBattlefieldAndReturn(player1, new BrutalCathar());
    }

    private Permanent castCathar(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Brutal Cathar");
    }

    private void addCatharMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
