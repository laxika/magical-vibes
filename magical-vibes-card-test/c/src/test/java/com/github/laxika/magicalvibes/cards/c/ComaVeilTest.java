package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ComaVeil.class, DruidOfTheAnima.class, ObeliskOfBant.class})
class ComaVeilTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Coma Veil")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new DruidOfTheAnima());

        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target an artifact with Coma Veil")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfBant());

        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Coma Veil attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new DruidOfTheAnima());

        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Coma Veil")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Tapped creature with Coma Veil does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new DruidOfTheAnima());
        creature.tap();

        Permanent veilPerm = new Permanent(new ComaVeil());
        veilPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(veilPerm);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped artifact with Coma Veil does not untap during controller's untap step")
    void enchantedArtifactDoesNotUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfBant());
        artifact.tap();

        Permanent veilPerm = new Permanent(new ComaVeil());
        veilPerm.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(veilPerm);

        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature can untap again after Coma Veil is removed")
    void creatureUntapsAfterVeilRemoved() {
        Permanent creature = addCreatureReady(player2, new DruidOfTheAnima());
        creature.tap();

        Permanent veilPerm = new Permanent(new ComaVeil());
        veilPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(veilPerm);

        gd.playerBattlefields.get(player1.getId()).remove(veilPerm);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfBant());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ComaVeil());
        enchantment.setAttachedTo(artifact.getId());
        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Resolving Coma Veil does not tap its target or lock other permanents")
    void doesNotTapTargetOrPreventOtherPermanentsUntapping() {
        Permanent creature = addCreatureReady(player1, new DruidOfTheAnima());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant());
        other.tap();
        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        creature.tap();
        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving Coma Veil attaches it to a noncreature artifact")
    void resolvingAttachesToArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfBant());
        harness.setHand(player1, List.of(new ComaVeil()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Coma Veil").getAttachedTo()).isEqualTo(artifact.getId());
    }
}
