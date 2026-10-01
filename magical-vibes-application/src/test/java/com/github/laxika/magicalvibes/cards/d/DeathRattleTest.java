package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathRattle.class, AugurOfSkulls.class, HorizonCanopy.class, Imperiosaur.class})
class DeathRattleTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and destroys a nongreen creature without allowing regeneration")
    void delvesAndDestroysNongreenCreatureWithoutRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AugurOfSkulls());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(target.getRegenerationShield()).isEqualTo(1);

        List<Card> graveyard = List.of(
                new Imperiosaur(), new Imperiosaur(), new Imperiosaur(),
                new Imperiosaur(), new Imperiosaur());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DeathRattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Augur of Skulls");
        harness.assertInGraveyard(player2, "Augur of Skulls");
        harness.assertInGraveyard(player1, "Death Rattle");
    }

    @Test
    @DisplayName("Cannot target a green creature")
    void cannotTargetGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        harness.setHand(player1, List.of(new DeathRattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nongreen creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HorizonCanopy());
        harness.setHand(player1, List.of(new DeathRattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nongreen creature");
    }
}
