package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DualCasting.class, Boomerang.class, CounselOfTheSoratami.class, GrizzlyBears.class, FountainOfYouth.class})
class DualCastingTest extends BaseCardTest {

    private Permanent enchant(com.github.laxika.magicalvibes.model.Player creatureController) {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(creatureController, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DualCasting());
        auraPerm.setAttachedTo(bearsPerm.getId());
        return bearsPerm;
    }

    @Test
    @DisplayName("Resolving Dual Casting attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        harness.addToBattlefield(player1, new FountainOfYouth());
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

        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        victim.setSummoningSick(false);

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
                .hasMessageContaining("Invalid ability index");
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature does not grant the ability to other creatures")
    void otherCreaturesDoNotGetTheAbility() {
        enchant(player1);

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherBears.setSummoningSick(false);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.activateAbility(player1, 0, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(otherBears.isTapped()).isFalse();
    }

    @Test
    void creatureControllerCanCopyTheirSpellWithOpponentsAura() {
        Permanent creature = enchant(player2);
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castSorcery(player2, 0, 0);
        harness.activateAbility(player2, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1)
                .allMatch(e -> e.getControllerId().equals(player2.getId()));
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void copyCanResolveWithNewTargetWithoutChangingOriginal() {
        enchant(player1);
        harness.setHand(player2, List.of());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Boomerang spell = new Boomerang();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, originalTarget.getId());
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget).doesNotContain(newTarget);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(originalTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(originalTarget);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void summoningSickCreatureCannotUseGrantedTapAbility() {
        Permanent creature = enchant(player1);
        creature.setSummoningSick(true);
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void activatedAbilityStillCopiesAfterAuraLeaves() {
        enchant(player1);
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, null, spell.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Dual Casting"));
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }
}
