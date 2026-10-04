package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.f.ForgeDevil;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({HotSoup.class, RuneclawBear.class, ForgeDevil.class, MagneticTheft.class, Naturalize.class})
class HotSoupTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Hot Soup to the target creature")
    void equipAttachesToCreature() {
        Permanent soup = addHotSoupReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(soup.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can't be blocked")
    void equippedCreatureCantBeBlocked() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures can still be blocked")
    void unequippedCreatureCanBeBlocked() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent other = addCreatureReady(player1, new RuneclawBear());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(creature.getId());

        assertThat(gqs.hasCantBeBlocked(gd, other)).isFalse();
    }

    @Test
    @DisplayName("Non-lethal damage to the equipped creature destroys it")
    void damageDestroysEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Hot Soup");
        assertThat(soup.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Damage to another creature does not destroy the equipped creature")
    void damageToOtherCreatureDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player2, new RuneclawBear());
        Permanent other = addCreatureReady(player2, new RuneclawBear());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(equipped.getId());

        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(equipped.getId()));
        assertThat(soup.getAttachedTo()).isEqualTo(equipped.getId());
    }

    @Test
    @DisplayName("Moving Hot Soup in response destroys the damaged creature, not the newly equipped creature")
    void movingEquipmentDoesNotChangeWhichCreatureIsDestroyed() {
        Permanent damaged = addCreatureReady(player2, new RuneclawBear());
        Permanent recipient = addCreatureReady(player2, new RuneclawBear());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(damaged.getId());

        harness.setHand(player1, List.of(new ForgeDevil(), new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, damaged.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(damaged.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, List.of(soup.getId(), recipient.getId()));
        assertThat(soup.getAttachedTo()).isEqualTo(recipient.getId());
        assertThat(gqs.hasCantBeBlocked(gd, damaged)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, recipient)).isTrue();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damaged).contains(recipient);
        assertThat(soup.getAttachedTo()).isEqualTo(recipient.getId());
    }

    @Test
    @DisplayName("Destroying Hot Soup in response does not stop destruction of the damaged creature")
    void destroyingEquipmentDoesNotStopTrigger() {
        Permanent damaged = addCreatureReady(player2, new RuneclawBear());
        Permanent soup = addHotSoupReady(player1);
        soup.setAttachedTo(damaged.getId());

        harness.setHand(player1, List.of(new ForgeDevil(), new Naturalize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, damaged.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(damaged.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, soup.getId());
        harness.assertInGraveyard(player1, "Hot Soup");
        assertThat(gqs.hasCantBeBlocked(gd, damaged)).isFalse();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent soup = addHotSoupReady(player1);
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(soup.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires three mana")
    void equipRejectsInsufficientMana() {
        Permanent soup = addHotSoupReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(soup.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated with a spell on the stack")
    void equipRequiresSorceryTiming() {
        Permanent soup = addHotSoupReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(soup.getAttachedTo()).isNull();
    }
    private Permanent addHotSoupReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HotSoup());
    }
}
