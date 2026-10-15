package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.cards.w.WuInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoisonArrow.class, WuInfantry.class, WeiInfantry.class, Plains.class})
class PoisonArrowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and gains 3 life")
    void destroysNonblackCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new WuInfantry());
        preparePoisonArrow();

        UUID targetId = harness.getPermanentId(player2, "Wu Infantry");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Wu Infantry");
        harness.assertInGraveyard(player2, "Wu Infantry");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new WuInfantry()); // legal target so the spell is playable
        harness.addToBattlefield(player2, new WeiInfantry());
        preparePoisonArrow();

        UUID targetId = harness.getPermanentId(player2, "Wei Infantry");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new WuInfantry()); // legal target so the spell is playable
        preparePoisonArrow();

        UUID targetId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("A creature with a regeneration shield is regenerated instead of destroyed")
    void regenerationShieldSavesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WuInfantry());
        target.setRegenerationShield(1);
        preparePoisonArrow();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Wu Infantry");
        harness.assertNotInGraveyard(player2, "Wu Infantry");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Does not gain life when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WuInfantry());
        preparePoisonArrow();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Poison Arrow");
    }

    @Test
    @DisplayName("Can destroy your own nonblack creature and still gain life")
    void destroysOwnCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WuInfantry());
        preparePoisonArrow();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Wu Infantry");
        harness.assertInGraveyard(player1, "Wu Infantry");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    private void preparePoisonArrow() {
        harness.setHand(player1, List.of(new PoisonArrow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
