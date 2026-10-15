package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RavineRaider;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fell.class, RavineRaider.class, Forest.class})
class FellTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target creature")
    void resolvingDestroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RavineRaider());
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Ravine Raider");
        harness.assertInGraveyard(player2, "Ravine Raider");
        harness.assertInGraveyard(player1, "Fell");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RavineRaider());
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Fell");
    }

    @Test
    @DisplayName("Can destroy a creature its caster controls")
    void destroysOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RavineRaider());
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Ravine Raider");
        harness.assertInGraveyard(player1, "Ravine Raider");
        harness.assertInGraveyard(player1, "Fell");
    }

    @Test
    @DisplayName("Regeneration replaces destruction and consumes the shield")
    void regenerationPreventsDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RavineRaider());
        creature.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Ravine Raider");
        harness.assertNotInGraveyard(player2, "Ravine Raider");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Fell");
    }
}
