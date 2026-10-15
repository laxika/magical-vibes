package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.m.MireKavu;
import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DralnusCrusade.class, MoggJailer.class, MireKavu.class, BoggartShenanigans.class})
class DralnusCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("Goblins get +1/+1, become black, and gain Zombie in addition to their types")
    void buffsAndChangesGoblins() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new MoggJailer());
        harness.addToBattlefield(player2, new DralnusCrusade());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, goblin))
                .containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, goblin))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Non-Goblins are not affected")
    void doesNotAffectNonGoblins() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        harness.addToBattlefield(player1, new DralnusCrusade());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactly(CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, kavu))
                .containsExactly(CardSubtype.KAVU);
    }

    @Test
    @DisplayName("Crusade affects Goblins entering later on either player's battlefield")
    void affectsLaterGoblinsForBothPlayers() {
        harness.addToBattlefield(player1, new DralnusCrusade());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new MoggJailer());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new MoggJailer());

        for (Permanent goblin : java.util.List.of(ownGoblin, opposingGoblin)) {
            assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
            assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
            assertThat(gqs.effectiveCreatureSubtypes(gd, goblin))
                    .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ZOMBIE);
        }
    }

    @Test
    @DisplayName("Noncreature Goblins become black and gain Zombie")
    void affectsNoncreatureGoblins() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new BoggartShenanigans());
        harness.addToBattlefield(player1, new DralnusCrusade());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, goblin))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Multiple Crusades stack their boosts and their effects end when they leave")
    void effectsStackAndEndWhenCrusadesLeave() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new MoggJailer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DralnusCrusade());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DralnusCrusade());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, goblin))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ZOMBIE);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, goblin))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ZOMBIE);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second));

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, goblin)).containsExactly(CardSubtype.GOBLIN);
    }
}
