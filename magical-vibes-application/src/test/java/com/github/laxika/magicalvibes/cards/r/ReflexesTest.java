package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reflexes.class, GrizzlyBears.class, Mountain.class})
class ReflexesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Reflexes puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Reflexes attaches it and grants first strike to the enchanted creature")
    void resolvingAttachesAndGrantsFirstStrike() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reflexes")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Reflexes does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent reflexesPerm = harness.addToBattlefieldAndReturn(player1, new Reflexes());
        reflexesPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses first strike when Reflexes leaves the battlefield")
    void creatureLosesFirstStrikeWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent reflexesPerm = harness.addToBattlefieldAndReturn(player1, new Reflexes());
        reflexesPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(reflexesPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Reflexes fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reflexes");
        harness.assertNotOnBattlefield(player1, "Reflexes");
    }

    @Test
    @DisplayName("Reflexes can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Reflexes()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
