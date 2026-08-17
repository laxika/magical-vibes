package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IzoniThousandEyedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Insect for each creature card in the controller's graveyard")
    void createsInsectsForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new IzoniThousandEyed()));
        addIzoniMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Izoni, Thousand-Eyed");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing another creature gains life and draws a card")
    void sacrificeAnotherCreatureGainsLifeAndDraws() {
        Permanent izoni = addReadyIzoni(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock()));
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(izoni);
    }

    @Test
    @DisplayName("Cannot sacrifice Izoni itself for its activated ability")
    void requiresAnotherCreature() {
        addReadyIzoni(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyIzoni(Player player) {
        Permanent perm = new Permanent(new IzoniThousandEyed());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addIzoniMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
