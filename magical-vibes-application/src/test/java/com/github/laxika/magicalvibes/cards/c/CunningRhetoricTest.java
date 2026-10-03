package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.Endbringer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningRhetoric.class, Divination.class, GrizzlyBears.class,
        JaceBeleren.class, Forest.class, Endbringer.class})
class CunningRhetoricTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card when multiple creatures attack and grants indefinite play permission")
    void exilesOneCardForAnAttack() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isFalse();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent attacks a planeswalker you control")
    void triggersForPlaneswalkerAttack() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        Permanent planeswalker = addTestPlaneswalker(player1);
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The controller can cast the exiled spell using mana of any color")
    void castsExiledSpellWithAnyColorMana() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new Divination(), new Divination()));
        harness.setHand(player1, List.of());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        prepareControllerMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An attack split between you and your planeswalker exiles only one card")
    void mixedAttackExilesOnlyOneCard() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        Permanent planeswalker = addTestPlaneswalker(player1);
        addReadyCreature(player2);
        addReadyCreature(player2);
        Card topCard = new Divination();
        Card nextCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        declareAttackers(player2, List.of(0, 1),
                Map.of(0, player1.getId(), 1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(nextCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("The controller's own attack does not trigger Cunning Rhetoric")
    void ownAttackDoesNotExile() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player1);
        Card topCard = new Divination();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty attacking player's library is harmless")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        harness.setLibrary(player2, List.of());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("The trigger still resolves after its source and attackers leave")
    void triggerSurvivesSourceAndAttackerRemoval() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An exiled land can be played even after Cunning Rhetoric leaves")
    void playsLandAfterSourceLeaves() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        prepareControllerMainPhase();
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(topCard.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Playing an exiled land still uses the normal land allowance")
    void cannotPlayLandAfterLandAllowanceUsed() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        prepareControllerMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Mana flexibility does not allow casting without enough mana")
    void cannotCastWithInsufficientMana() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        prepareControllerMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Mana of any color cannot replace a required colorless mana symbol")
    void cannotPayColorlessCostWithColoredMana() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Endbringer();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        prepareControllerMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Exile permission does not let a sorcery be cast during the opponent's combat")
    void sorceryTimingStillApplies() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Permission survives cleanup and can be used on the controller's next turn")
    void canCastOnLaterTurn() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(topCard.getId()));
    }

    @Test
    @DisplayName("The attacking player cannot use the controller's exile permission")
    void ownerCannotCastExiledCard() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    private void prepareControllerMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addReadyCreature(Player player) {
        addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addTestPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        return planeswalker;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
