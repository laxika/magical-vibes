package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FencersMagemark;
import com.github.laxika.magicalvibes.cards.o.OrderOfTheStars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiltratorsMagemark.class, FencersMagemark.class, IzzetGuildmage.class,
        OrderOfTheStars.class})
class InfiltratorsMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Infiltrator's Magemark attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IzzetGuildmage());

        harness.setHand(player1, List.of(new InfiltratorsMagemark()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof InfiltratorsMagemark
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Infiltrator's Magemark boosts each enchanted creature you control")
    void boostsEnchantedCreaturesYouControl() {
        Permanent firstBears = addCreatureReady(player1, new IzzetGuildmage());
        Permanent secondBears = addCreatureReady(player1, new IzzetGuildmage());
        Permanent unenchantedBears = addCreatureReady(player1, new IzzetGuildmage());
        Permanent opponentBears = addCreatureReady(player2, new IzzetGuildmage());
        addAura(firstBears);
        addAura(secondBears);

        assertThat(gqs.getEffectivePower(gd, firstBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Infiltrator's Magemark boosts your creatures enchanted by any Aura")
    void boostsCreaturesEnchantedByAnyAuraYouControl() {
        Permanent otherEnchantedCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent magemarkEnchantedCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent opponentCreature = addCreatureReady(player2, new IzzetGuildmage());
        addAura(magemarkEnchantedCreature);

        Permanent otherAura = harness.addToBattlefieldAndReturn(player2, new FencersMagemark());
        otherAura.setAttachedTo(otherEnchantedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, otherEnchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherEnchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, magemarkEnchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, magemarkEnchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Infiltrator's Magemark stops boosting when it leaves the battlefield")
    void bonusStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent magemark = harness.addToBattlefieldAndReturn(player1, new InfiltratorsMagemark());
        magemark.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(magemark);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Infiltrator's Magemark fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IzzetGuildmage());

        harness.setHand(player1, List.of(new InfiltratorsMagemark()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof InfiltratorsMagemark);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof InfiltratorsMagemark);
    }

    @Test
    @DisplayName("Infiltrator's Magemark cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InfiltratorsMagemark());
        harness.setHand(player1, List.of(new InfiltratorsMagemark()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An enchanted creature cannot be blocked by a creature without defender")
    void enchantedCreatureCannotBeBlockedByNormalCreature() {
        Permanent attacker = addCreatureReady(player1, new IzzetGuildmage());
        addAura(attacker);
        attacker.setAttacking(true);
        addCreatureReady(player2, new IzzetGuildmage());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("An enchanted creature can be blocked by a creature with defender")
    void enchantedCreatureCanBeBlockedByDefender() {
        Permanent attacker = addCreatureReady(player1, new IzzetGuildmage());
        addAura(attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new OrderOfTheStars());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unenchanted creature is unaffected by Infiltrator's Magemark")
    void unenchantedCreatureIsUnaffected() {
        Permanent attacker = addCreatureReady(player1, new IzzetGuildmage());
        Permanent enchantedCreature = addCreatureReady(player1, new IzzetGuildmage());
        addAura(enchantedCreature);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IzzetGuildmage());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Infiltrator's Magemark does not affect enchanted creatures controlled by an opponent")
    void onlyAffectsEnchantedCreaturesYouControl() {
        Permanent attacker = addCreatureReady(player2, new IzzetGuildmage());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new IzzetGuildmage());
        addAura(attacker);
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Aura also makes your creature subject to Magemark's blocking restriction")
    void opponentAuraEnablesBlockingRestriction() {
        Permanent attacker = addCreatureReady(player1, new IzzetGuildmage());
        Permanent magemarkCreature = addCreatureReady(player1, new IzzetGuildmage());
        addAura(magemarkCreature);
        Permanent otherAura = harness.addToBattlefieldAndReturn(player2, new FencersMagemark());
        otherAura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new IzzetGuildmage());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("Losing the last Aura removes both bonuses while Magemark remains on the battlefield")
    void losingLastAuraRemovesBonuses() {
        Permanent attacker = addCreatureReady(player1, new IzzetGuildmage());
        Permanent magemarkCreature = addCreatureReady(player1, new IzzetGuildmage());
        addAura(magemarkCreature);
        Permanent otherAura = harness.addToBattlefieldAndReturn(player2, new FencersMagemark());
        otherAura.setAttachedTo(attacker.getId());
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId()).remove(otherAura);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IzzetGuildmage());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private void addAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InfiltratorsMagemark());
        aura.setAttachedTo(enchantedCreature.getId());
    }

}
