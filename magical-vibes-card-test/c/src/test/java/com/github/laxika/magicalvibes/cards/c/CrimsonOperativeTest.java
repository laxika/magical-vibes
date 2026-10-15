package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TimeStretch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimsonOperative.class, GrizzlyBears.class, Mountain.class, Shock.class, TimeStretch.class})
class CrimsonOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with play permission through the owner's next turn")
    void etbExilesTopCardUntilNextTurn() {
        Card top = new Shock();
        Card below = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, below));

        castCrimsonOperative();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(top.getId()))
                .isEqualTo(Integer.MAX_VALUE);
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("ETB does nothing when the library is empty")
    void etbDoesNothingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castCrimsonOperative();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Prowess boosts Crimson Operative for a noncreature spell")
    void prowessBoostsForNoncreatureSpell() {
        Permanent operative = addReadyOperative();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess does not trigger for a creature spell")
    void prowessDoesNotTriggerForCreatureSpell() {
        Permanent operative = addReadyOperative();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
    }

    @Test
    void exiledSpellCanBeCastForItsNormalCostAndTriggersProwessOnce() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        castCrimsonOperative();
        Permanent operative = findPermanent(player1, "Crimson Operative");
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passUntilWithNoAttackers(null, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(3);
    }

    @Test
    void exiledLandCanBePlayedWithoutTriggeringProwess() {
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        castCrimsonOperative();
        Permanent operative = findPermanent(player1, "Crimson Operative");

        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent operative = addReadyOperative();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passUntilWithNoAttackers(null, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
    }

    @Test
    void multipleProwessBoostsAccumulateAndExpireAtEndOfTurn() {
        Permanent operative = addReadyOperative();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passUntilWithNoAttackers(null, TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passUntilWithNoAttackers(null, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(2);
    }

    @Test
    void permissionLastsThroughNextNormalTurnAndThenExpires() {
        Card top = prepareExiledSpellWithLibrariesForTurnProgression();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void permissionExpiresAtEndOfControllersFirstExtraTurn() {
        Card top = prepareExiledSpellWithLibrariesForTurnProgression();
        giveExtraTurnsTo(player1);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void opponentsExtraTurnsDoNotExpirePermissionBeforeControllersNextTurn() {
        Card top = prepareExiledSpellWithLibrariesForTurnProgression();
        giveExtraTurnsTo(player2);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passUntilWithNoAttackers(null, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    private Card prepareExiledSpellWithLibrariesForTurnProgression() {
        harness.setHand(player2, List.of());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castCrimsonOperative();
        return top;
    }

    private void giveExtraTurnsTo(Player player) {
        harness.setHand(player1, List.of(new TimeStretch()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castSorcery(player1, 0, player.getId());
        harness.passUntilWithNoAttackers(null, TurnStep.POSTCOMBAT_MAIN);
    }

    private void castCrimsonOperative() {
        harness.castFromHand(player1, new CrimsonOperative(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyOperative() {
        Permanent operative = addCreatureReady(player1, new CrimsonOperative());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return operative;
    }
}
