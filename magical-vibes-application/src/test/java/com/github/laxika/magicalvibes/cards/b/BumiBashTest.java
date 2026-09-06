package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BumiBash.class, FieldOfRuin.class, Forest.class, GiantSpider.class, Plains.class, TreetopVillage.class})
class BumiBashTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode uses the caster's current land count")
    void damageModeUsesCurrentLandCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BumiBash()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Destroy mode destroys a nonbasic land")
    void destroyModeDestroysNonbasicLand() {
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.setHand(player1, List.of(new BumiBash()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Field of Ruin"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Field of Ruin");
    }

    @Test
    @DisplayName("Destroy mode can target a land creature")
    void destroyModeDestroysLandCreature() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new BumiBash()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 1, village.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Treetop Village");
    }

    @Test
    @DisplayName("Destroy mode cannot target a basic noncreature land")
    void destroyModeCannotTargetBasicNoncreatureLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new BumiBash()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land creature or nonbasic land");
    }
}
