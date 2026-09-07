package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorthyKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RosethornHalberd.class, GrizzlyBears.class, WorthyKnight.class})
class RosethornHalberdTest extends BaseCardTest {

    @Test
    @DisplayName("Rosethorn Halberd enters attached to a targeted non-Human creature")
    void entersAttachedToTargetNonHumanCreature() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RosethornHalberd()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent halberd = findPermanent(player1, "Rosethorn Halberd");
        assertThat(halberd.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Rosethorn Halberd does not target a Human creature on entry")
    void doesNotTargetHumanCreatureOnEntry() {
        harness.addToBattlefield(player1, new WorthyKnight());
        harness.setHand(player1, List.of(new RosethornHalberd()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent halberd = findPermanent(player1, "Rosethorn Halberd");
        assertThat(halberd.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Rosethorn Halberd rejects a Human creature as its entry target")
    void rejectsHumanCreatureAsEntryTarget() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new WorthyKnight());
        harness.setHand(player1, List.of(new RosethornHalberd()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Human creature");
    }

    @Test
    @DisplayName("Equip attaches Rosethorn Halberd to a non-Human creature")
    void equipAttachesToNonHumanCreature() {
        Permanent halberd = addReadyHalberd(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equip cannot target a Human creature")
    void equipCannotTargetHumanCreature() {
        addReadyHalberd(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new WorthyKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Human creature");
    }

    private Permanent addReadyHalberd(Player player) {
        Permanent halberd = new Permanent(new RosethornHalberd());
        halberd.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(halberd);
        return halberd;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
