package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfForgeAndFrontier.class, GrizzlyBears.class, Forest.class})
class SwordOfForgeAndFrontierTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from red and green")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage exiles the top two cards for play and grants an extra land play")
    void combatDamageExilesTopTwoAndGrantsExtraLandPlay() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        Card top = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second));
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(top.getId(), second.getId());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("An unattached Sword does not trigger from combat damage")
    void unattachedSwordDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1);
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches the Sword to a creature")
    void equipsToCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void emptyLibraryStillGrantsAdditionalLandPlay() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    void singleCardLibraryExilesOnlyAvailableCard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(creature.getId());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    void swordControllerReceivesBenefitsWhenOpponentsCreatureDealsDamage() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(creature.getId());
        Card top = new Forest();
        Card second = new Forest();
        Card opponentsTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opponentsTop));
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTop);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(creature.getId());
        Card spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(spell, new Forest()));
        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void additionalLandMayBePlayedFromExileAfterNormalLandPlay() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(creature.getId());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionsExpireButCardsRemainExiledOnNextTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(creature.getId());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
    }

    @Test
    void twoEquippedAttackersGrantTwoAdditionalLandPlays() {
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1).setAttachedTo(firstCreature.getId());
        addSwordReady(player1).setAttachedTo(secondCreature.getId());
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        firstCreature.setAttacking(true);
        secondCreature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
    }
    private Permanent addSwordReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new SwordOfForgeAndFrontier());
    }
}