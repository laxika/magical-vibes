package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ScorchingShot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchmagesNewt.class, Shock.class, ScorchingShot.class})
class ArchmagesNewtTest extends BaseCardTest {

    @Test
    @DisplayName("A saddled Archmage's Newt grants free flashback")
    void saddledGrantsFreeFlashback() {
        Shock shock = new Shock();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        newt.setSaddled(true);
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shock));

        resolveCombatAndChoose(shock);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("An unsaddled Archmage's Newt grants flashback for the card's mana cost")
    void unsaddledGrantsNormalFlashbackCost() {
        Shock shock = new Shock();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shock));

        resolveCombatAndChoose(shock);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Saddle 3 taps other creatures with total power at least three")
    void saddleTapsOtherCreatures() {
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArchmagesNewt());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArchmagesNewt());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(newt.isTapped()).isFalse();
        assertThat(newt.isSaddled()).isTrue();
    }

    @Test
    @DisplayName("The Newt cannot count its own power toward saddle 3")
    void insufficientOtherPowerCannotSaddle() {
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        Permanent helper = addCreatureReady(player1, new ArchmagesNewt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(newt.isSaddled()).isFalse();
        assertThat(helper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Saddle can only be activated at sorcery speed")
    void saddleCannotBeActivatedDuringCombat() {
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        addCreatureReady(player1, new ArchmagesNewt());
        addCreatureReady(player1, new ArchmagesNewt());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(newt.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("A saddled Newt grants free flashback to a sorcery")
    void saddledGrantsSorceryFlashback() {
        ScorchingShot shot = new ScorchingShot();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        Permanent target = addCreatureReady(player2, new ArchmagesNewt());
        newt.setSaddled(true);
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shot));

        resolveCombatAndChoose(shot);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shot);
    }

    @Test
    @DisplayName("The combat trigger cannot target a creature card")
    void cannotChooseCreatureCard() {
        ArchmagesNewt creatureCard = new ArchmagesNewt();
        ScorchingShot shot = new ScorchingShot();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(creatureCard, shot));

        resolveCombat();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(shot.getId()));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The combat trigger requires a target when a legal card exists")
    void cannotDeclineRequiredTarget() {
        ScorchingShot shot = new ScorchingShot();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shot));

        resolveCombat();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(shot.getId()));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Granted flashback expires at the end of the turn")
    void flashbackExpiresAtEndOfTurn() {
        ScorchingShot shot = new ScorchingShot();
        Permanent newt = addCreatureReady(player1, new ArchmagesNewt());
        Permanent target = addCreatureReady(player2, new ArchmagesNewt());
        newt.setSaddled(true);
        newt.setAttacking(true);
        harness.setGraveyard(player1, List.of(shot));

        resolveCombatAndChoose(shot);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shot);
    }

    private void resolveCombatAndChoose(Card card) {
        resolveCombat();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        resolveAllTriggers();
    }
}
