package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LargeBear;
import com.github.laxika.magicalvibes.cards.s.SmaugTheGreatCalamity;
import com.github.laxika.magicalvibes.cards.s.SpewFlame;
import com.github.laxika.magicalvibes.cards.w.WizardsStaff;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranduilsDecree.class, LargeBear.class, TidingsOfWar.class})
class ThranduilsDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a permanent spell, exiles it, and lets Decree's controller cast it for free")
    void countersPermanentSpellAndGrantsFreeCastPermission() {
        LargeBear bears = new LargeBear();
        castDecreeAgainstCreature(bears);

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        harness.assertNotInGraveyard(player2, "Large Bear");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Large Bear");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Counters a nonpermanent spell into its owner's graveyard")
    void countersNonpermanentSpellIntoGraveyard() {
        TidingsOfWar tidings = new TidingsOfWar();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(tidings));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new ThranduilsDecree()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player2, 0, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, tidings.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Tidings of War");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(tidings.getId());
    }

    @Test
    @CardUsed({SmaugTheGreatCalamity.class, SpewFlame.class})
    void counteredAdventureGoesToGraveyardWithoutCastingPermission() {
        SmaugTheGreatCalamity smaug = new SmaugTheGreatCalamity();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new LargeBear());
        harness.setHand(player2, List.of(smaug));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setHand(player1, List.of(new ThranduilsDecree()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAdventure(player2, 0, harness.getPermanentId(player1, "Large Bear"));
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, smaug.getId());

        harness.assertInGraveyard(player2, "Smaug, the Great Calamity");
        assertThat(gd.findExiledCard(smaug.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(smaug.getId());
        harness.assertOnBattlefield(player1, "Large Bear");
    }

    @Test
    @CardUsed(WizardsStaff.class)
    void countersNoncreaturePermanentAndAllowsFreeCast() {
        WizardsStaff staff = new WizardsStaff();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(staff));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setHand(player1, List.of(new ThranduilsDecree()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, staff.getId());

        assertThat(gd.findExiledCard(staff.getId())).isNotNull();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, staff.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wizard's Staff");
        assertThat(gd.findExiledCard(staff.getId())).isNull();
    }

    @Test
    void ownerDoesNotReceiveDecreesCastingPermission() {
        LargeBear bears = new LargeBear();
        castDecreeAgainstCreature(bears);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void freeCastingPermissionDoesNotOverrideCreatureTiming() {
        LargeBear bears = new LargeBear();
        castDecreeAgainstCreature(bears);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void freeCastingPermissionSurvivesTurnChange() {
        LargeBear bears = new LargeBear();
        castDecreeAgainstCreature(bears);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Large Bear");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void permissionEndsWhenCardLeavesExile() {
        LargeBear bears = new LargeBear();
        castDecreeAgainstCreature(bears);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(bears.getId());
    }

    private void castDecreeAgainstCreature(LargeBear bears) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new ThranduilsDecree()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
    }
}
