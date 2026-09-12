package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reflexes.class, GoblinRaider.class, Mountain.class})
class ReflexesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Reflexes puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        Permanent target = addCreatureReady(player1, new GoblinRaider());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Reflexes attaches it and grants first strike to the enchanted creature")
    void resolvingAttachesAndGrantsFirstStrike() {
        Permanent target = addCreatureReady(player1, new GoblinRaider());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reflexes")
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Reflexes does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = addCreatureReady(player1, new GoblinRaider());
        Permanent otherCreature = addCreatureReady(player1, new GoblinRaider());

        Permanent reflexesPerm = harness.addToBattlefieldAndReturn(player1, new Reflexes());
        reflexesPerm.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses first strike when Reflexes leaves the battlefield")
    void creatureLosesFirstStrikeWhenRemoved() {
        Permanent target = addCreatureReady(player1, new GoblinRaider());

        Permanent reflexesPerm = harness.addToBattlefieldAndReturn(player1, new Reflexes());
        reflexesPerm.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(reflexesPerm);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Reflexes fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player1, new GoblinRaider());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reflexes");
        harness.assertNotOnBattlefield(player1, "Reflexes");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GoblinRaider());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Reflexes can enchant a creature an opponent controls")
    void canEnchantCreatureOpponentControls() {
        Permanent opponentCreature = addCreatureReady(player2, new GoblinRaider());
        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isTrue();
    }
}
