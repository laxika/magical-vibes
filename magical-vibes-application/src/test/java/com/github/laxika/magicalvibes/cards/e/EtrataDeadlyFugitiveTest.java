package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DemandAnswers;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.InnocentBystander;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KarlovWatchdog;
import com.github.laxika.magicalvibes.cards.r.RepeatOffender;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtrataDeadlyFugitive.class, InnocentBystander.class, Divination.class, Island.class,
        KarlovWatchdog.class, RepeatOffender.class, DemandAnswers.class})
class EtrataDeadlyFugitiveTest extends BaseCardTest {

    @Test
    void assassinCombatDamageCloaksDamagedPlayersTopCardUnderItsControllersControl() {
        Card topCard = new InnocentBystander();
        Permanent cloaked = resolveEtrataTrigger(topCard);

        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(cloaked.getCard()).isSameAs(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloaked);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(cloaked);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    void nonAssassinCombatDamageDoesNotTriggerCloak() {
        harness.addToBattlefield(player1, new EtrataDeadlyFugitive());
        Permanent attacker = addCreatureReady(player1, new InnocentBystander());
        attacker.setAttacking(true);
        Card topCard = new InnocentBystander();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isCloaked);
    }

    @Test
    void cloakedCreatureCanTurnFaceUpForTheGrantedAbilityCost() {
        Permanent cloaked = resolveEtrataTrigger(new InnocentBystander());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked), 0, null, null);
        harness.passBothPriorities();

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(cloaked.isCloaked()).isFalse();
    }

    @Test
    void instantOrSorceryIsExiledAndMayBeCastForFreeWhenItCannotTurnFaceUp() {
        Card topCard = new Divination();
        Permanent cloaked = resolveEtrataTrigger(topCard);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked), 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloaked);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Divination");
    }

    @Test
    void landTurnsFaceUpAndStaysOnTheBattlefield() {
        Card land = new Island();
        Permanent cloaked = resolveEtrataTrigger(land);
        payAndActivateGrantedAbility(cloaked);
        harness.passBothPriorities();

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cloaked);
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void decliningFreeCastLeavesTheCardInExile() {
        Card sorcery = new Divination();
        Permanent cloaked = resolveEtrataTrigger(sorcery);
        payAndActivateGrantedAbility(cloaked);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloaked);
        assertThat(gd.findExiledCard(sorcery.getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Divination");
    }

    @Test
    void anotherControlledAssassinAlsoTriggersCloak() {
        harness.addToBattlefield(player1, new EtrataDeadlyFugitive());
        Permanent attacker = addCreatureReady(player1, new RepeatOffender());
        attacker.setAttacking(true);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isCloaked() && permanent.getCard() == topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void opposingAssassinDoesNotTriggerCloak() {
        harness.addToBattlefield(player1, new EtrataDeadlyFugitive());
        Permanent attacker = addCreatureReady(player2, new RepeatOffender());
        attacker.setAttacking(true);
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isCloaked);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(Permanent::isCloaked);
    }

    @Test
    void emptyDamagedPlayersLibraryDoesNotCreateACloakedPermanent() {
        Permanent attacker = addCreatureReady(player1, new EtrataDeadlyFugitive());
        attacker.setAttacking(true);
        harness.setLibrary(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isCloaked);
    }

    @Test
    void preventedFaceUpActionExilesCreatureAndAllowsFreeCast() {
        Card creature = new InnocentBystander();
        Permanent cloaked = resolveEtrataTrigger(creature);
        harness.addToBattlefield(player2, new KarlovWatchdog());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        payAndActivateGrantedAbility(cloaked);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloaked);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> !permanent.isFaceDown() && permanent.getCard() == creature);
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void secondActivationExilesAndOffersToCastTheNowFaceUpCreature() {
        Card creature = new InnocentBystander();
        Permanent cloaked = resolveEtrataTrigger(creature);
        payAndActivateGrantedAbility(cloaked);
        payAndActivateGrantedAbility(cloaked);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloaked);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> !permanent.isFaceDown() && permanent.getCard() == creature);
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void freeCastCannotPutSpellOnStackBeforeItsMandatoryAdditionalCostIsPaid() {
        Card spell = new DemandAnswers();
        Card discard = new Island();
        Permanent cloaked = resolveEtrataTrigger(spell);
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        payAndActivateGrantedAbility(cloaked);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == spell
                && gd.playerHands.get(player1.getId()).contains(discard));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card == spell
                && gd.playerHands.get(player1.getId()).contains(discard));
    }

    @Test
    void freeCastWithUnpayableAdditionalCostLeavesTheCardExiled() {
        Card spell = new DemandAnswers();
        Permanent cloaked = resolveEtrataTrigger(spell);
        harness.setHand(player1, List.of());
        payAndActivateGrantedAbility(cloaked);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == spell);
    }

    private void payAndActivateGrantedAbility(Permanent cloaked) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cloaked), 0, null, null);
    }

    private Permanent resolveEtrataTrigger(Card topCard) {
        harness.setLibrary(player2, List.of(topCard, new Island()));
        Permanent etrata = addCreatureReady(player1, new EtrataDeadlyFugitive());
        etrata.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .findFirst()
                .orElseThrow();
    }
}
