package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StenchOfEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every Plains and leaves other lands and creatures alone")
    void destroysOnlyPlains() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castStenchOfEvil();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the payment deals 1 damage per land destroyed")
    void decliningDealsOneDamagePerLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 2);
    }

    @Test
    @DisplayName("Each land is a separate payment — paying {2} once still leaves the other land's damage")
    void payingOnceAvoidsOnlyThatLandsDamage() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 1);
    }

    @Test
    @DisplayName("The caster's own Plains are destroyed and the caster is asked to pay too")
    void casterIsNotSpared() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Plains"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1 - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 1);
    }

    @Test
    @DisplayName("No Plains on the battlefield means no payment prompt and no damage")
    void noPlainsNoPrompt() {
        harness.addToBattlefield(player2, new Forest());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2);
    }

    private void castStenchOfEvil() {
        harness.setHand(player1, List.of(new StenchOfEvil()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
