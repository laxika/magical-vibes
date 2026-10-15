package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.w.WorthyKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RosethornHalberd.class, Gingerbrute.class, WorthyKnight.class})
class RosethornHalberdTest extends BaseCardTest {

    @Test
    @DisplayName("Rosethorn Halberd enters attached to a targeted non-Human creature")
    void entersAttachedToTargetNonHumanCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new RosethornHalberd()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent halberd = findPermanent(player1, "Rosethorn Halberd");
        assertThat(halberd.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
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
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new RosethornHalberd());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip can target a Human creature")
    void equipCanTargetHumanCreature() {
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new RosethornHalberd());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new WorthyKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new RosethornHalberd());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Re-equipping transfers the bonus to the new creature")
    void reequippingTransfersBonus() {
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new RosethornHalberd());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}
