package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlyVisit.class, Plains.class, ShuFootSoldiers.class, WeiInfantry.class})
class GhostlyVisitTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ghostly Visit targeting a nonblack creature puts it on stack")
    void castingPutsOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new GhostlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        // A legal nonblack target so the spell itself is playable.
        harness.addToBattlefield(player1, new ShuFootSoldiers());

        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());

        harness.setHand(player1, List.of(new GhostlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new ShuFootSoldiers());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new GhostlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Resolving Ghostly Visit destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new GhostlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertInGraveyard(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Ghostly Visit can be regenerated (no regeneration prevention)")
    void canBeRegenerated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        creature.setRegenerationShield(1);

        harness.setHand(player1, List.of(new GhostlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
        harness.assertNotInGraveyard(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Ghostly Visit fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new GhostlyVisit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghostly Visit");
    }
}
