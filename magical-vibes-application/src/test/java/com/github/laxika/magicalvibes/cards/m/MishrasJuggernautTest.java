package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RazeToTheGround;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasJuggernaut.class, RazeToTheGround.class, MachineOverMatter.class})
class MishrasJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Mishra's Juggernaut must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new MishrasJuggernaut());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Unearth returns Mishra's Juggernaut with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new MishrasJuggernaut()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent juggernaut = findPermanent(player1, "Mishra's Juggernaut");
        assertThat(juggernaut.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Mishra's Juggernaut");
    }

    @Test
    @DisplayName("Unearthed Mishra's Juggernaut is exiled at the next end step")
    void unearthExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new MishrasJuggernaut()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mishra's Juggernaut");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Mishra's Juggernaut"));
    }

    @Test
    void tappedJuggernautNeedNotAttack() {
        Permanent juggernaut = addCreatureReady(player1, new MishrasJuggernaut());
        juggernaut.tap();

        declareAttackers(List.of());

        assertThat(juggernaut.isAttacking()).isFalse();
    }

    @Test
    void summoningSickJuggernautNeedNotAttack() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new MishrasJuggernaut());

        declareAttackers(List.of());

        assertThat(juggernaut.isAttacking()).isFalse();
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        prepareUnearth();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
    }

    @Test
    void unearthCannotBeActivatedDuringOpponentsTurn() {
        prepareUnearth();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
    }

    @Test
    void unearthRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new MishrasJuggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
        harness.assertNotOnBattlefield(player1, "Mishra's Juggernaut");
    }

    @Test
    void unearthRequiresFiveGenericManaInAdditionToRed() {
        harness.setGraveyard(player1, List.of(new MishrasJuggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
        harness.assertNotOnBattlefield(player1, "Mishra's Juggernaut");
    }

    @Test
    void unearthCannotBeActivatedWithSpellOnStack() {
        prepareUnearth();
        Permanent target = addCreatureReady(player2, new MishrasJuggernaut());
        harness.setHand(player1, List.of(new RazeToTheGround()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
    }

    @Test
    void unearthedJuggernautCanAttackImmediately() {
        prepareUnearth();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    void unearthedJuggernautIsExiledInsteadOfBeingDestroyed() {
        prepareUnearth();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent juggernaut = findPermanent(player1, "Mishra's Juggernaut");
        harness.setHand(player1, List.of(new RazeToTheGround()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, juggernaut.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mishra's Juggernaut");
        harness.assertNotInGraveyard(player1, "Mishra's Juggernaut");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(juggernaut.getCard().getId()));
    }

    @Test
    void normallyCastJuggernautGoesToGraveyardWhenDestroyed() {
        harness.setHand(player1, List.of(new MishrasJuggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent juggernaut = findPermanent(player1, "Mishra's Juggernaut");
        harness.setHand(player1, List.of(new RazeToTheGround()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, juggernaut.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void unearthedJuggernautIsExiledInsteadOfReturningToHand() {
        prepareUnearth();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent juggernaut = findPermanent(player1, "Mishra's Juggernaut");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, juggernaut.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mishra's Juggernaut");
        harness.assertNotInGraveyard(player1, "Mishra's Juggernaut");
        harness.assertNotInHand(player1, "Mishra's Juggernaut");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(juggernaut.getCard().getId()));
    }

    @Test
    void unearthReturnsOnlyTheActivatedCard() {
        MishrasJuggernaut first = new MishrasJuggernaut();
        MishrasJuggernaut second = new MishrasJuggernaut();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCard().getId()).isEqualTo(second.getId()));
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MishrasJuggernaut());
        Permanent blocker = addCreatureReady(player2, new MishrasJuggernaut());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Mishra's Juggernaut");
        harness.assertInGraveyard(player2, "Mishra's Juggernaut");
    }

    private void prepareUnearth() {
        harness.setGraveyard(player1, List.of(new MishrasJuggernaut()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
