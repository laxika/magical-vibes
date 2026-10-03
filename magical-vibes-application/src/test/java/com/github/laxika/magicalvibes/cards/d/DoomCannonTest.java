package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinPiledriver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomCannon.class, GoblinPiledriver.class, ElvishWarrior.class})
class DoomCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Doom Cannon enters stores the choice")
    void choosesCreatureTypeAsItEnters() {
        harness.castFromHand(player1, new DoomCannon(), "{6}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Doom Cannon").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Sacrificing a creature of the chosen type can deal 3 damage to a creature")
    void dealsDamageToCreatureTarget() {
        Permanent cannon = addReadyCannon(CardSubtype.GOBLIN);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        goblin.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(cannon.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Goblin Piledriver");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Sacrificing a creature of the chosen type deals 3 damage to any target")
    void sacrificesChosenTypeAndDealsDamage() {
        Permanent cannon = addReadyCannon(CardSubtype.GOBLIN);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        goblin.setSummoningSick(false);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(cannon.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Goblin Piledriver");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A creature of another type cannot be sacrificed for Doom Cannon")
    void cannotSacrificeCreatureOfAnotherType() {
        Permanent cannon = addReadyCannon(CardSubtype.GOBLIN);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        elf.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature of the chosen type");

        assertThat(cannon.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("A newly entered Cannon can sacrifice a tapped, summoning-sick creature")
    void canActivateImmediatelyAndSacrificeTappedCreature() {
        harness.castFromHand(player1, new DoomCannon(), "{6}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        goblin.setSummoningSick(true);
        goblin.setTapped(true);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Goblin Piledriver");
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Doom Cannon").isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay Doom Cannon's sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent cannon = addReadyCannon(CardSubtype.GOBLIN);
        harness.addToBattlefield(player2, new GoblinPiledriver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature of the chosen type");

        assertThat(cannon.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Goblin Piledriver");
    }

    @Test
    @DisplayName("Doom Cannon can choose Warrior and deal damage to its controller")
    void canChooseSharedSecondaryTypeAndTargetController() {
        harness.castFromHand(player1, new DoomCannon(), "{6}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WARRIOR");
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertLife(player1, 17);
    }

    private Permanent addReadyCannon(CardSubtype chosenSubtype) {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new DoomCannon());
        cannon.setChosenSubtype(chosenSubtype);
        cannon.setSummoningSick(false);
        return cannon;
    }
}
