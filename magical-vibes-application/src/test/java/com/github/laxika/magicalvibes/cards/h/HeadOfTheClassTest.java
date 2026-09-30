package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeadOfTheClass.class, BarkshellBlessing.class, DoomBlade.class, GrizzlyBears.class})
class HeadOfTheClassTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature-targeting spell each turn costs one white and one black less")
    void reducesFirstCreatureTargetingSpellOnly() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID firstTargetId = harness.getPermanentId(player1, "Grizzly Bears");
        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .toList();
        UUID secondTargetId = bears.get(1).getId();

        harness.castInstant(player1, 0, firstTargetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, secondTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Repartee perpetually boosts Head of the Class")
    void reparteePerpetuallyBoostsThisCreature() {
        harness.addToBattlefield(player1, new HeadOfTheClass());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent head = findPermanent(player1, "Head of the Class");
        assertThat(gqs.getEffectivePower(gd, head)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, head)).isEqualTo(3);
    }
}
