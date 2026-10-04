package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhaveGuruOfSpores.class, GrizzlyBears.class, FountainOfYouth.class})
class GhaveGuruOfSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five +1/+1 counters")
    void entersWithFiveCounters() {
        harness.castFromHand(player1, new GhaveGuruOfSpores(), "{2}{W}{B}{G}");
        harness.passBothPriorities();

        Permanent ghave = findPermanent(player1, "Ghave, Guru of Spores");

        assertThat(ghave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removes a counter to create a Saproling token")
    void removesCounterToCreateSaproling() {
        Permanent ghave = addReadyGhave();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ghave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices a creature to put a counter on the target creature")
    void sacrificesCreatureToPutCounterOnTarget() {
        Permanent ghave = addReadyGhave();
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ghave.getId()));
    }

    @Test
    @DisplayName("The sacrifice ability only targets creatures")
    void sacrificeAbilityRejectsNoncreatureTarget() {
        addReadyGhave();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can remove a counter from another controlled creature as a cost")
    void removesCounterFromAnotherCreatureBeforeResolution() {
        Permanent ghave = addReadyGhave();
        Permanent donor = addCreatureReady(player1, new GrizzlyBears());
        donor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, donor.getId());

        assertThat(donor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ghave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(countPermanents(player1, "Saproling")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, token)).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Cannot remove an opponent's counter to pay the token ability")
    void cannotRemoveOpponentsCounter() {
        addReadyGhave();
        Permanent donor = addCreatureReady(player1, new GrizzlyBears());
        donor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.handlePermanentChosen(player1, donor.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Ghave's last counter does not stop its token ability")
    void tokenAbilityResolvesAfterGhaveDies() {
        Permanent ghave = addReadyGhave();
        ghave.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ghave, Guru of Spores")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ghave.getCard());
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    @DisplayName("Ghave can sacrifice itself and the counter ability still resolves")
    void sacrificesGhaveToPutCounterOnAnotherCreature() {
        Permanent ghave = addReadyGhave();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(countPermanents(player1, "Ghave, Guru of Spores")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ghave.getCard());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ghave can target itself while sacrificing another creature")
    void putsCounterOnGhave() {
        Permanent ghave = addReadyGhave();
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, ghave.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(ghave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
    }

    @Test
    @DisplayName("Sacrificing the target pays the cost but leaves no legal target")
    void canSacrificeTheTargetCreature() {
        Permanent ghave = addReadyGhave();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(ghave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both abilities work while Ghave is tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent ghave = addReadyGhave();
        ghave.setSummoningSick(true);
        ghave.tap();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent saproling = findPermanent(player1, "Saproling");

        harness.activateAbility(player1, 0, 1, null, ghave.getId());
        harness.handlePermanentChosen(player1, saproling.getId());
        harness.passBothPriorities();

        assertThat(ghave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(ghave.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    private Permanent addReadyGhave() {
        Permanent ghave = addCreatureReady(player1, new GhaveGuruOfSpores());
        ghave.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        return ghave;
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
