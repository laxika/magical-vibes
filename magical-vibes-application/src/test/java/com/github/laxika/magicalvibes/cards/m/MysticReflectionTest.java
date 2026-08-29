package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MysticReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("The next creature enters as a copy of the targeted creature")
    void nextCreatureEntersAsTargetCopy() {
        harness.addToBattlefield(player1, new Archangel());
        castMysticReflection(harness.getPermanentId(player1, "Archangel"));

        Permanent bears = castBears();

        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
        assertThat(bears.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The replacement is consumed after one entry event")
    void replacementIsConsumedAfterOneEntryEvent() {
        harness.addToBattlefield(player1, new Archangel());
        castMysticReflection(harness.getPermanentId(player1, "Archangel"));

        Permanent firstBears = castBears();
        Permanent secondBears = castBears();

        assertThat(firstBears.getCard().getName()).isEqualTo("Archangel");
        assertThat(secondBears.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A target that leaves the battlefield is still copied using last-known information")
    void targetLeavingBattlefieldStillProvidesCopyValues() {
        harness.addToBattlefield(player1, new Archangel());
        castMysticReflection(harness.getPermanentId(player1, "Archangel"));
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Archangel"));

        Permanent bears = castBears();

        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
    }

    private void castMysticReflection(UUID targetId) {
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent castBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
