package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlueMagesCane.class, Divination.class, GrizzlyBears.class})
class BlueMagesCaneTest extends BaseCardTest {

    @Test
    void jobSelectCreatesAndEquipsWizardHero() {
        harness.setHand(player1, List.of(new BlueMagesCane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cane = findPermanent(player1, "Blue Mage's Cane");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(cane.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.WIZARD);
    }

    @Test
    void equippedCreatureAttacksToCopyAndCastDefendingGraveyardSpellForThree() {
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new BlueMagesCane());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        cane.setAttachedTo(attacker.getId());

        Divination opponentDivination = new Divination();
        Divination ownDivination = new Divination();
        Card invalidCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownDivination));
        harness.setGraveyard(player2, List.of(opponentDivination, invalidCreature));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        attackWith(player1, attacker);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentDivination.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentDivination.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentDivination);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(ownDivination);
    }

    @Test
    void mayChooseNoGraveyardTargetEvenWhenOneIsAvailable() {
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new BlueMagesCane());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        cane.setAttachedTo(attacker.getId());
        Divination spell = new Divination();
        harness.setGraveyard(player2, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        attackWith(player1, attacker);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningCopyLeavesOriginalExiledAndRemovesCopy() {
        harness.setHand(player1, List.of());
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new BlueMagesCane());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        cane.setAttachedTo(attacker.getId());
        Divination spell = new Divination();
        harness.setGraveyard(player2, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        attackWith(player1, attacker);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void equipMovesBonusesAndWizardTypeToNewCreature() {
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new BlueMagesCane());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        cane.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(cane.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.WIZARD);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.BEAR, CardSubtype.WIZARD);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void attackAbilityStillCopiesAfterEquipmentLeavesBattlefield() {
        harness.setHand(player1, List.of());
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new BlueMagesCane());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        cane.setAttachedTo(attacker.getId());
        Divination spell = new Divination();
        harness.setGraveyard(player2, List.of(spell));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        attackWith(player1, attacker);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(cane);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
    }

    @Test
    void removedGraveyardTargetIsNotCopied() {
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new BlueMagesCane());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        cane.setAttachedTo(attacker.getId());
        Divination spell = new Divination();
        harness.setGraveyard(player2, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        attackWith(player1, attacker);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void attackWith(Player player, Permanent attacker) {
        declareAttackers(player, List.of(gd.playerBattlefields.get(player.getId()).indexOf(attacker)));
        harness.passBothPriorities();
    }
}
