package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Tidings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PotionersTrove.class, Cancel.class, GrizzlyBears.class, Tidings.class})
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

    @Test
    @DisplayName("A sorcery on the stack enables the life ability before resolving")
    void sorceryEnablesLifeAbilityBeforeResolving() {
        Permanent trove = harness.addToBattlefieldAndReturn(player1, new PotionersTrove());
        harness.setLife(player1, 18);
        harness.castFromHand(player1, new Tidings(), "{3}{U}{U}");

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, 18);
        assertThat(gd.stack).hasSize(2);
        assertThat(trove.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's instant does not enable the life ability")
    void opponentsInstantDoesNotEnableLifeAbility() {
        Permanent trove = addReadyTrove();
        gd.recordSpellCast(player2.getId(), new Cancel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery spell");
        assertThat(trove.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An instant cast last turn does not enable the life ability")
    void lastTurnsInstantDoesNotEnableLifeAbility() {
        addReadyTrove();
        gd.recordSpellCast(player1.getId(), new Cancel());
        gd.snapshotSpellCountsAndClear(gd.spellsCastLastTurn);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery spell");
    }

    @Test
    @DisplayName("Using the mana ability prevents paying the life ability's tap cost")
    void manaAndLifeAbilitiesShareTapCost() {
        addReadyTrove();
        gd.recordSpellCast(player1.getId(), new Cancel());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyTrove() {
        Permanent trove = harness.addToBattlefieldAndReturn(player1, new PotionersTrove());
        trove.setSummoningSick(false);
        return trove;
    }
}
