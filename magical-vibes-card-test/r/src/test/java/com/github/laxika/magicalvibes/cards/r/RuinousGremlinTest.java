package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuinousGremlin.class, PropheticPrism.class, Mountain.class})
class RuinousGremlinTest extends BaseCardTest {

    @Test
    void sacrificesItselfToDestroyTargetArtifact() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new RuinousGremlin());
        gremlin.setSummoningSick(false);
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Prophetic Prism");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gremlin);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getOriginalCard() instanceof PropheticPrism);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof RuinousGremlin);
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new RuinousGremlin());
        gremlin.setSummoningSick(false);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesImmediatelyAndResolvesWhileSummoningSickAndTapped() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new RuinousGremlin());
        gremlin.tap();
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Prophetic Prism"));

        harness.assertNotOnBattlefield(player1, "Ruinous Gremlin");
        harness.assertInGraveyard(player1, "Ruinous Gremlin");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertNotInGraveyard(player2, "Prophetic Prism");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertInGraveyard(player2, "Prophetic Prism");
    }

    @Test
    void canDestroyItsControllersArtifact() {
        harness.addToBattlefield(player1, new RuinousGremlin());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Prophetic Prism"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Ruinous Gremlin");
    }

    @Test
    void cannotActivateWithoutEnoughGenericMana() {
        harness.addToBattlefield(player1, new RuinousGremlin());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player2, "Prophetic Prism");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ruinous Gremlin");
        harness.assertNotInGraveyard(player1, "Ruinous Gremlin");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTheRedRequirementWithColorlessMana() {
        harness.addToBattlefield(player1, new RuinousGremlin());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID targetId = harness.getPermanentId(player2, "Prophetic Prism");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ruinous Gremlin");
        harness.assertNotInGraveyard(player1, "Ruinous Gremlin");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.stack).isEmpty();
    }
}
