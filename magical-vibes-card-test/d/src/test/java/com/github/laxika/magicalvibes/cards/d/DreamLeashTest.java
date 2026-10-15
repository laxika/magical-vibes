package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeaveNoTrace;
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

@CardUsed({DreamLeash.class, BorosRecruit.class, Forest.class, LeaveNoTrace.class})
class DreamLeashTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Dream Leash steals a tapped permanent")
    void stealsTappedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        creature.tap();

        castDreamLeash(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dream Leash")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Dream Leash steals a tapped noncreature permanent")
    void stealsTappedNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        castDreamLeash(forest);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Cannot target an untapped permanent")
    void cannotTargetUntappedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        assertThatThrownBy(() -> castDreamLeash(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped permanent");
    }

    @Test
    @DisplayName("Can resolve after the target becomes untapped")
    void resolvesAfterTargetBecomesUntapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        creature.tap();

        castDreamLeash(creature);
        creature.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The permanent returns to its owner when Dream Leash leaves")
    void permanentReturnsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        creature.tap();

        castDreamLeash(creature);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dream Leash");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LeaveNoTrace()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Control continues when the enchanted permanent untaps")
    void retainsControlAfterUntapping() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        creature.tap();

        castDreamLeash(creature);
        harness.passBothPriorities();
        creature.untap();
        harness.runStateBasedActions();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(findPermanent(player1, "Dream Leash").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Changing Dream Leash's controller changes control of the enchanted permanent")
    void enchantedPermanentFollowsAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        creature.tap();

        castDreamLeash(creature);
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Dream Leash");
        aura.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DreamLeash()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura, creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura, creature);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void castDreamLeash(Permanent target) {
        harness.setHand(player1, List.of(new DreamLeash()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, target.getId());
    }
}
