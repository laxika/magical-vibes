package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SpringjawTrap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythosOfIlluna.class, GrizzlyBears.class, HillGiant.class, SpringjawTrap.class})
class MythosOfIllunaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of the target permanent")
    void createsTokenCopyOfTargetPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("With red and green mana, the token fights up to one opposing creature")
    void enhancedCopyFightsTargetedOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities();

        assertThat(opponentGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    @DisplayName("The enhanced token can decline its optional fight")
    void enhancedCopyCanDeclineFight() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(opponentGiant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Red mana alone does not grant the fight ability")
    void redWithoutGreenDoesNotGrantFight() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Green mana alone does not grant the fight ability")
    void greenWithoutRedDoesNotGrantFight() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An enhanced noncreature copy does not trigger the fight ability")
    void enhancedNoncreatureCopyDoesNotTriggerFight() {
        Permanent trap = harness.addToBattlefieldAndReturn(player2, new SpringjawTrap());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new MythosOfIlluna()));
        addEnhancedMana();

        harness.castAndResolveSorcery(player1, 0, trap.getId());

        harness.assertOnBattlefield(player1, "Springjaw Trap");
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copying an enhanced token preserves its fight ability without red and green payment")
    void copyingEnhancedTokenPreservesFightAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MythosOfIlluna(), new MythosOfIlluna()));
        addEnhancedMana();

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        Permanent firstToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getId().equals(bears.getId())).findFirst().orElseThrow();
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, firstToken.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, firstToken);
    }

    private void addEnhancedMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
