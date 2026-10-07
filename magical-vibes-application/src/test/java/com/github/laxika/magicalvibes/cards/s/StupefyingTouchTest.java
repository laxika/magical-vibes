package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.CephalidIllusionist;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StupefyingTouch.class, AngelOfRetribution.class, CephalidIllusionist.class, TaintedIsle.class})
class StupefyingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Stupefying Touch enters attached to a creature and draws a card")
    void entersAttachedAndDrawsCard() {
        Permanent creature = addCreatureReady(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.setLibrary(player1, List.of(new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Stupefying Touch").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInHand(player1, "Angel of Retribution");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = addCreatureReady(player1, new CephalidIllusionist());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StupefyingTouch());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Stupefying Touch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature's triggered abilities still trigger")
    void triggeredAbilitiesStillWork() {
        Permanent creature = addCreatureReady(player1, new CephalidIllusionist());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StupefyingTouch());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new AngelOfRetribution(),
                new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.setLibrary(player2, List.of(new AngelOfRetribution()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new StupefyingTouch()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player2, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInHand(player2, "Angel of Retribution");
    }

    @Test
    @DisplayName("Removing the Aura restores the creature's activated abilities")
    void removingAuraRestoresActivatedAbilities() {
        Permanent creature = addCreatureReady(player1, new CephalidIllusionist());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StupefyingTouch());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new AngelOfRetribution(),
                new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());
        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An Aura with an illegal target does not enter or draw a card")
    void illegalTargetAtResolutionDoesNotDraw() {
        Permanent creature = addCreatureReady(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.setLibrary(player1, List.of(new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Stupefying Touch");
        harness.assertInGraveyard(player1, "Stupefying Touch");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
