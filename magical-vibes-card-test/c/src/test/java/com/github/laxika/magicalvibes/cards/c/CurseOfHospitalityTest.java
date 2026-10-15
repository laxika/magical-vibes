package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ActOfAggression;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CurseOfHospitality.class, Divination.class, GrizzlyBears.class, Island.class, ActOfAggression.class})
class CurseOfHospitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures attacking the enchanted player have trample")
    void attackingCreaturesHaveTrample() {
        placeCurseOnPlayer2();
        Permanent attacker = addAttacker(player1, player2);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addAttacker(player2, player1);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card face up for the creature controller until end of turn")
    void combatDamageGrantsEndOfTurnPlayPermission() {
        Permanent curse = placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isFalse();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(entry.exilerId()).isNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).doesNotContain(topCard.getId());
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The creature controller can cast the exiled spell using mana of any color")
    void creatureControllerCanCastExiledSpellWithAnyMana() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast + 2);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Curse can be cast attached to a player")
    void canEnchantPlayer() {
        harness.setHand(player1, List.of(new CurseOfHospitality()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of Hospitality").getAttachedTo())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Each damaging creature exiles one card, regardless of damage amount")
    void eachCreatureExilesOneCard() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        addAttacker(player1, player2);
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player2, List.of(first, second, third));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.findExiledCard(third.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.exilePlayPermissions.get(first.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(second.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Damage to an unenchanted player does not exile a card")
    void damageToOtherPlayerDoesNotTrigger() {
        Permanent curse = placeCurseOnPlayer2();
        curse.setAttachedTo(player1.getId());
        addAttacker(player1, player2);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library exiles nothing and does not cause a loss")
    void emptyLibraryDoesNothing() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The creature controller can play an exiled land during their main phase")
    void canPlayExiledLand() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card land = new Island();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    @DisplayName("Exiled sorceries still require sorcery timing")
    void permissionDoesNotOverrideTiming() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card spell = new Divination();
        harness.setLibrary(player2, List.of(spell));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("Unplayed cards remain exiled after permission expires at cleanup")
    void permissionExpiresAtCleanup() {
        placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Removing the curse after damage does not stop its trigger")
    void triggerSurvivesCurseLeaving() {
        Permanent curse = placeCurseOnPlayer2();
        addAttacker(player1, player2);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        gd.playerGraveyards.get(player1.getId()).add(curse.getCard());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Play permission belongs to the creature's controller when the trigger resolves")
    void permissionUsesControllerAtResolution() {
        placeCurseOnPlayer2();
        Permanent attacker = addAttacker(player1, player2);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player2, List.of(new ActOfAggression()));

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castInstant(player2, 0, attacker.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player2.getId());
    }

    private Permanent placeCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfHospitality());
        curse.setAttachedTo(player2.getId());
        return curse;
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player attacker,
                                  com.github.laxika.magicalvibes.model.Player defender) {
        Permanent creature = addCreatureReady(attacker, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(defender.getId());
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
