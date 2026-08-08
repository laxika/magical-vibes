package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PusKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices Pus Kami and destroys a nonblack creature")
    void destroysNonblackCreature() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pus Kami");
        harness.assertInGraveyard(player1, "Pus Kami");
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Ability cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new BogImp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Bog Imp")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Soulshift 6 returns a targeted Spirit with mana value 6 or less when Pus Kami dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
