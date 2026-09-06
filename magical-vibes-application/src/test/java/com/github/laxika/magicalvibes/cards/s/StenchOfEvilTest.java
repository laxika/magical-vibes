package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StenchOfEvil.class, SnowCoveredPlains.class, SnowCoveredForest.class,
        BalduvianBears.class, CircleOfProtectionBlack.class})
class StenchOfEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every Plains and leaves other lands and creatures alone")
    void destroysOnlyPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        castStenchOfEvil();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactlyInAnyOrder(forest, bears)
                .doesNotContain(plains);
    }

    @Test
    @DisplayName("Declining the payment deals 1 damage per land destroyed")
    void decliningDealsOneDamagePerLand() {
        harness.addToBattlefield(player2, new SnowCoveredPlains());
        harness.addToBattlefield(player2, new SnowCoveredPlains());
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
        harness.addToBattlefield(player2, new SnowCoveredPlains());
        harness.addToBattlefield(player2, new SnowCoveredPlains());
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
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(ownPlains);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentPlains);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1 - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 1);
    }

    @Test
    @DisplayName("No Plains on the battlefield means no payment prompt and no damage")
    void noPlainsNoPrompt() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2);
    }

    @Test
    @DisplayName("An indestructible Plains is not counted for the damage rider")
    void indestructiblePlainsAreNotCounted() {
        Permanent indestructiblePlains =
                harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
        indestructiblePlains.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new SnowCoveredPlains());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        castStenchOfEvil();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructiblePlains);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The damage rider uses normal damage and can be prevented")
    void damageCanBePrevented() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CircleOfProtectionBlack());
        harness.addToBattlefield(player2, new SnowCoveredPlains());
        StenchOfEvil stench = new StenchOfEvil();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(stench));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player2, 0, 0);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(stench.getId());
        harness.handlePermanentChosen(player2, stench.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
    }

    private void castStenchOfEvil() {
        harness.castFromHand(player1, new StenchOfEvil(), "{2}{B}{B}");
        harness.passBothPriorities();
    }
}
