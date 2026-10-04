package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnthrallingHold.class, WalkingCorpse.class, ReturnToNature.class})
class EnthrallingHoldTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Enthralling Hold steals a tapped creature")
    void resolvingStealsTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.tap();

        castEnthrallingHold(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Enthralling Hold")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new EnthrallingHold()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Still resolves if the target becomes untapped before resolution")
    void resolvesAfterTargetBecomesUntapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.tap();

        castEnthrallingHold(creature);
        creature.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Enthralling Hold")
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.tap();

        castEnthrallingHold(creature);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enthralling Hold");
    }

    @Test
    @DisplayName("Creature returns to its owner when Enthralling Hold leaves")
    void creatureReturnsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.tap();

        castEnthrallingHold(creature);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Enthralling Hold");
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, 1, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Control and attachment persist when the enchanted creature untaps")
    void keepsControlAfterUntapping() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.tap();
        castEnthrallingHold(creature);
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(findPermanent(player1, "Enthralling Hold").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Can enchant a tapped creature already controlled by the caster")
    void canEnchantOwnTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        creature.tap();
        castEnthrallingHold(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(findPermanent(player1, "Enthralling Hold").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Removing the newer control Aura restores the older Aura's controller")
    void removingNewerAuraRestoresOlderControlEffect() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.tap();
        castEnthrallingHold(creature);
        harness.passBothPriorities();
        Permanent olderAura = findPermanent(player1, "Enthralling Hold");

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new EnthrallingHold()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(olderAura).doesNotContain(creature);
        Permanent newerAura = findPermanent(player2, "Enthralling Hold");
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 1, newerAura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, olderAura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature, newerAura);
        assertThat(olderAura.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void castEnthrallingHold(Permanent target) {
        harness.setHand(player1, List.of(new EnthrallingHold()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, target.getId());
    }
}
