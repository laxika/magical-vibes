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

@CardUsed({DeclarationOfNaught.class, GrizzlyBears.class, HillGiant.class})
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

    private Permanent declaration(Player player) {
        return findPermanent(player, "Declaration of Naught");
    }

    private Permanent addReadyDeclaration(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeclarationOfNaught());
        perm.setChosenName(chosenName);
        return perm;
    }
}
