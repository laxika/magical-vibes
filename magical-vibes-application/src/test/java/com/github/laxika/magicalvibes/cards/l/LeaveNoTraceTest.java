package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.ConcertedEffort;
import com.github.laxika.magicalvibes.cards.d.DarkHeartOfTheWood;
import com.github.laxika.magicalvibes.cards.g.GlareOfSubdual;
import com.github.laxika.magicalvibes.cards.g.GolgariGermination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeaveNoTrace.class, DarkHeartOfTheWood.class, GlareOfSubdual.class,
        GolgariGermination.class, ConcertedEffort.class, BorosRecruit.class})
class LeaveNoTraceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target and every other enchantment sharing a color with it")
    void destroysTargetAndColorSharingEnchantments() {
        harness.addToBattlefield(player2, new DarkHeartOfTheWood());
        harness.addToBattlefield(player1, new GlareOfSubdual());
        harness.addToBattlefield(player2, new GolgariGermination());
        harness.addToBattlefield(player2, new ConcertedEffort());
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new LeaveNoTrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Dark Heart of the Wood");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dark Heart of the Wood");
        harness.assertInGraveyard(player2, "Dark Heart of the Wood");
        harness.assertNotOnBattlefield(player1, "Glare of Subdual");
        harness.assertInGraveyard(player1, "Glare of Subdual");
        harness.assertNotOnBattlefield(player2, "Golgari Germination");
        harness.assertInGraveyard(player2, "Golgari Germination");
        harness.assertOnBattlefield(player2, "Concerted Effort");
        harness.assertOnBattlefield(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("Can target only an enchantment")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new LeaveNoTrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Boros Recruit");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
