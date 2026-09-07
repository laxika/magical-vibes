package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiledriver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomCannon.class, GoblinPiledriver.class, GrizzlyBears.class})
class DoomCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Doom Cannon enters stores the choice")
    void choosesCreatureTypeAsItEnters() {
        harness.setHand(player1, List.of(new DoomCannon()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Doom Cannon").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
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
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature of the chosen type");

        assertThat(cannon.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyCannon(CardSubtype chosenSubtype) {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new DoomCannon());
        cannon.setChosenSubtype(chosenSubtype);
        cannon.setSummoningSick(false);
        return cannon;
    }
}
