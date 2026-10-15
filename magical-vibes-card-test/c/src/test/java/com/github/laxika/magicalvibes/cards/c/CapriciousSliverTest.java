package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapriciousSliver.class, SinewSliver.class, GrizzlyBears.class, Mountain.class})
class CapriciousSliverTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepTurnStepsAvailableForAssertions() {
        for (var player : java.util.List.of(player1, player2)) {
            gd.playerAutoStopSteps.put(player.getId(), java.util.EnumSet.of(
                    com.github.laxika.magicalvibes.model.TurnStep.UPKEEP,
                    com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                    com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN,
                    com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
        }
    }


    @Test
    @DisplayName("A Sliver's combat damage exiles the top card of its controller's library for play")
    void sliverCombatDamageExilesTopCardForPlay() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addAttackingCreature(player1, new CapriciousSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("The ability is granted to other Slivers you control")
    void otherSliverAlsoExilesTopCardForPlay() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new CapriciousSliver());
        addAttackingCreature(player1, new SinewSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A non-Sliver does not gain the ability")
    void nonSliverDoesNotExileTopCard() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new CapriciousSliver());
        addAttackingCreature(player1, new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Each Sliver that deals combat damage exiles a separate top card")
    void eachSliverTriggersSeparately() {
        Card firstTopCard = new GrizzlyBears();
        Card secondTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));
        addAttackingCreature(player1, new SinewSliver());
        addAttackingCreature(player1, new CapriciousSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstTopCard.getId(), secondTopCard.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstTopCard.getId(), player1.getId())
                .containsEntry(secondTopCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Opposing Slivers do not gain the ability")
    void opposingSliverDoesNotExileTopCard() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        addCreatureReady(player1, new CapriciousSliver()).tap();
        addAttackingCreature(player2, new SinewSliver());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Multiple Capricious Slivers grant separate instances of the ability")
    void multipleCopiesGrantMultipleTriggers() {
        Card firstTopCard = new GrizzlyBears();
        Card secondTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));
        addCreatureReady(player1, new CapriciousSliver());
        addCreatureReady(player1, new CapriciousSliver());
        addAttackingCreature(player1, new SinewSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstTopCard.getId(), secondTopCard.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstTopCard.getId(), player1.getId())
                .containsEntry(secondTopCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("An empty library leaves no card or play permission")
    void emptyLibraryExilesNothing() {
        harness.setLibrary(player1, List.of());
        addAttackingCreature(player1, new CapriciousSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A combat damage trigger still resolves after Capricious Sliver leaves")
    void triggerResolvesAfterSourceLeaves() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent sliver = addAttackingCreature(player1, new CapriciousSliver());

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sliver);
        gd.playerGraveyards.get(player1.getId()).add(sliver.getCard());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("An exiled creature can be cast in the second main phase by paying its cost")
    void exiledCreatureCanBeCastWithNormalManaCost() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addAttackingCreature(player1, new CapriciousSliver());
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The permission does not waive the exiled card's mana cost")
    void exiledCreatureCannotBeCastWithoutMana() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addAttackingCreature(player1, new CapriciousSliver());
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The exiled card may be played as a land")
    void exiledLandCanBePlayed() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        addAttackingCreature(player1, new CapriciousSliver());
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("The permission does not allow a creature spell during combat")
    void normalSpellTimingStillApplies() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addAttackingCreature(player1, new CapriciousSliver());
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Unplayed cards stay exiled but their play permission expires after the turn")
    void permissionExpiresAfterTurn() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addAttackingCreature(player1, new CapriciousSliver());
        resolveCombat();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
