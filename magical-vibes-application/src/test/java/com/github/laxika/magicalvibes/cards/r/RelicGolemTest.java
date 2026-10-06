package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicGolem.class, Spellbook.class, GrizzlyBears.class})
class RelicGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack unless an opponent has eight cards in their graveyard")
    void cannotAttackBelowOpponentGraveyardThreshold() {
        Permanent relicGolem = addReadyRelicGolem(player1);
        fillGraveyard(player1, 8);
        fillGraveyard(player2, 7);
        harness.addToBattlefield(player2, new GrizzlyBears());

        beginAttackerDeclaration(player1);

        int relicGolemIndex = gd.playerBattlefields.get(player1.getId()).indexOf(relicGolem);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(relicGolemIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack when an opponent has eight cards in their graveyard")
    void canAttackAtOpponentGraveyardThreshold() {
        Permanent relicGolem = addReadyRelicGolem(player1);
        fillGraveyard(player2, 8);
        harness.addToBattlefield(player2, new GrizzlyBears());

        beginAttackerDeclaration(player1);

        int relicGolemIndex = gd.playerBattlefields.get(player1.getId()).indexOf(relicGolem);
        gs.declareAttackers(gd, player1, List.of(relicGolemIndex));

        assertThat(relicGolem.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block unless an opponent has eight cards in their graveyard")
    void cannotBlockBelowOpponentGraveyardThreshold() {
        addReadyRelicGolem(player2);
        fillGraveyard(player1, 7);
        addAttackingCreature(player1);

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Can block when an opponent has eight cards in their graveyard")
    void canBlockAtOpponentGraveyardThreshold() {
        addReadyRelicGolem(player2);
        fillGraveyard(player1, 8);
        addAttackingCreature(player1);

        beginBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Activated ability mills two cards from the target player's library")
    void activatedAbilityMillsTwoCards() {
        addReadyRelicGolem(player1);
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook(), new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void canMillItsControllerBelowGraveyardThreshold() {
        Permanent golem = addReadyRelicGolem(player1);
        Card first = new Spellbook();
        Card second = new Spellbook();
        Card remaining = new Spellbook();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(golem.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, remaining);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void millsOnlyAvailableCardFromShortLibrary() {
        addReadyRelicGolem(player1);
        Card onlyCard = new Spellbook();
        harness.setLibrary(player2, List.of(onlyCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new RelicGolem());
        golem.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(golem.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        Permanent golem = addReadyRelicGolem(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(golem.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addReadyRelicGolem(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void millingOpponentEnablesAnotherGolemToAttack() {
        addReadyRelicGolem(player1);
        Permanent attacker = addReadyRelicGolem(player1);
        fillGraveyard(player2, 6);
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook(), new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        beginAttackerDeclaration(player1);
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void losingGraveyardThresholdBeforeDeclarationPreventsAttack() {
        Permanent golem = addReadyRelicGolem(player1);
        fillGraveyard(player2, 8);
        fillGraveyard(player2, 7);
        beginAttackerDeclaration(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(golem.isAttacking()).isFalse();
    }

    private Permanent addReadyRelicGolem(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RelicGolem());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private void beginAttackerDeclaration(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void addAttackingCreature(Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
