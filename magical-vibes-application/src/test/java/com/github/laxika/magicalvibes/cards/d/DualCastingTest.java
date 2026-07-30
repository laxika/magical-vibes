package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DualCastingTest extends BaseCardTest {

    private Permanent enchant(com.github.laxika.magicalvibes.model.Player creatureController) {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(creatureController.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new DualCasting());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return bearsPerm;
    }

    @Test
    @DisplayName("Resolving Dual Casting attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new DualCasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dual Casting")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, List.of(new DualCasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID artifactId = findPermanent(player1, "Fountain of Youth").getId();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature taps for {R} to copy an instant or sorcery spell you control")
    void grantedAbilityCopiesOwnSpell() {
        Permanent bearsPerm = enchant(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, null, counsel.getId());

        // Resolve the granted ability -> one copy of Counsel of the Soratami
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        StackEntry copyEntry = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the retarget prompt keeps the copy on the original target")
    void copyKeepsOriginalTargetWhenRetargetDeclined() {
        enchant(player1);

        Permanent victim = new Permanent(new GrizzlyBears());
        victim.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(victim);

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.activateAbility(player1, 0, null, boomerang.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .hasSize(1)
                .allMatch(e -> victim.getId().equals(e.getTargetId()));
    }

    @Test
    @DisplayName("Cannot copy a spell controlled by another player")
    void cannotCopyOpponentSpell() {
        enchant(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);

        UUID counselId = counsel.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, counselId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot copy a creature spell")
    void cannotCopyCreatureSpell() {
        enchant(player1);

        GrizzlyBears bearsSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(bearsSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        UUID bearsSpellId = bearsSpell.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsSpellId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature loses the granted ability when Dual Casting leaves the battlefield")
    void abilityLostWhenAuraRemoved() {
        Permanent bearsPerm = enchant(player1);
        Permanent auraPerm = findPermanent(player1, "Dual Casting");

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        UUID counselId = counsel.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, counselId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature does not grant the ability to other creatures")
    void otherCreaturesDoNotGetTheAbility() {
        enchant(player1);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.activateAbility(player1, 0, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(otherBears.isTapped()).isFalse();
    }
}
