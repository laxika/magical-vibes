package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({DeclarationOfNaught.class, GrizzlyBears.class, HillGiant.class, Disperse.class})
class DeclarationOfNaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a card name on enter records it on the permanent")
    void etbChoosesCardName() {
        harness.setHand(player1, List.of(new DeclarationOfNaught()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(declaration(player1).getChosenName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a spell with the chosen name")
    void countersSpellWithChosenName() {
        addReadyDeclaration(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLUE, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter a matching spell cast by its controller")
    void countersMatchingSpellCastByController() {
        addReadyDeclaration(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLUE, 2);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a spell whose name is not the chosen name")
    void cannotTargetOtherNamedSpell() {
        addReadyDeclaration(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLUE, 1);

        HillGiant giant = new HillGiant();
        harness.setHand(player2, List.of(giant));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The name is chosen before the enchantment enters, without a triggered ability")
    void choosesNameBeforeEntering() {
        harness.setHand(player1, List.of(new DeclarationOfNaught()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Declaration of Naught");
        harness.handleListChoice(player1, "Declaration of Naught");

        harness.assertOnBattlefield(player1, "Declaration of Naught");
        assertThat(declaration(player1).getChosenName()).isEqualTo("Declaration of Naught");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a matching enchantment even after the source returns to hand")
    void countersMatchingEnchantmentAfterSourceLeaves() {
        Permanent source = addReadyDeclaration(player1, "Declaration of Naught");
        DeclarationOfNaught spell = new DeclarationOfNaught();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Declaration of Naught");
        harness.assertNotOnBattlefield(player1, "Declaration of Naught");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Declaration of Naught");
        harness.assertNotOnBattlefield(player2, "Declaration of Naught");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A matching spell cannot be countered without paying blue mana")
    void requiresBlueManaToActivate() {
        addReadyDeclaration(player1, "Declaration of Naught");
        DeclarationOfNaught spell = new DeclarationOfNaught();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player2, "Declaration of Naught");
    }

    private Permanent declaration(Player player) {
        return findPermanent(player, "Declaration of Naught");
    }

    private Permanent addReadyDeclaration(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeclarationOfNaught());
        perm.setChosenName(chosenName);
        return perm;
    }
}
