package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Deathlace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Paralyze;
import com.github.laxika.magicalvibes.cards.t.TheRack;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackWard.class, Deathlace.class, GrizzlyBears.class, Paralyze.class, TheRack.class, WhiteKnight.class})
class BlackWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from black")
    void enchantedCreatureHasProtectionFromBlack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BlackWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not gain protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BlackWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Black Ward leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BlackWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLACK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection from black does not remove Black Ward when it becomes black")
    void protectionDoesNotRemoveBlackWardWhenWardBecomesBlack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlackWard());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gqs.getEffectiveColors(gd, aura)).containsExactly(CardColor.BLACK);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Resolving Black Ward attaches it and grants protection from black")
    void resolvesAndGrantsProtectionFromBlack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlackWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == BlackWard.class
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlackWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TheRack());
        harness.setHand(player1, List.of(new BlackWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Independent protection from black removes Black Ward if it becomes black")
    void independentProtectionRemovesBlackWardWhenItBecomesBlack() {
        Permanent knight = addCreatureReady(player1, new WhiteKnight());
        harness.setHand(player1, List.of(new BlackWard(), new Deathlace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, knight.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Black Ward");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertNotOnBattlefield(player1, "Black Ward");
        harness.assertInGraveyard(player1, "Black Ward");
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Black Ward removes an already attached black Aura")
    void protectionRemovesOtherBlackAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent paralyze = harness.addToBattlefieldAndReturn(player2, new Paralyze());
        paralyze.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new BlackWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Ward");
        harness.assertNotOnBattlefield(player2, "Paralyze");
        harness.assertInGraveyard(player2, "Paralyze");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Protection from Black Ward prevents targeting by a black spell")
    void protectionPreventsBlackSpellTargeting() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlackWard());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
