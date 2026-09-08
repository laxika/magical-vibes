package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PotionersTroveTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void addsManaOfChosenColor() {
        Permanent trove = addReadyTrove();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(trove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The life ability cannot be activated before casting an instant or sorcery")
    void cannotGainLifeWithoutInstantOrSorcery() {
        addReadyTrove();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery spell");
    }

    @Test
    @DisplayName("A creature spell does not enable the life ability")
    void creatureSpellDoesNotEnableLifeAbility() {
        addReadyTrove();
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery spell");
    }

    @Test
    @DisplayName("The life ability gains two life after casting an instant")
    void gainsTwoLifeAfterCastingInstant() {
        Permanent trove = addReadyTrove();
        gd.recordSpellCast(player1.getId(), new Cancel());
        harness.setLife(player1, 18);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(trove.isTapped()).isTrue();
    }

    private Permanent addReadyTrove() {
        Permanent trove = harness.addToBattlefieldAndReturn(player1, new PotionersTrove());
        trove.setSummoningSick(false);
        return trove;
    }
}
