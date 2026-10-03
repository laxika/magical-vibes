package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.cards.s.ShieldOfDutyAndReason;
import com.github.laxika.magicalvibes.cards.s.StandardBearer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoalitionHonorGuard.class, AngelfireCrusader.class, DegaDisciple.class, Jilt.class,
        ShieldOfDutyAndReason.class, StandardBearer.class})
class CoalitionHonorGuardTest extends BaseCardTest {

    @Test
    void opponentMustTargetHonorGuardWhenAble() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.castInstant(player2, 0, honorGuard.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(honorGuard.getId());
    }

    @Test
    void opponentMustTargetHonorGuardWithActivatedAbilityWhenAble() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent disciple = addCreatureReady(player2, new DegaDisciple());

        int discipleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(disciple);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, discipleIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.activateAbility(player2, discipleIndex, null, honorGuard.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(honorGuard.getId());
    }

    @Test
    void controllerIsNotForcedToTargetItsOwnHonorGuard() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player1, List.of(new Jilt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(otherCreature.getId());
        assertThat(honorGuard.getId()).isNotEqualTo(otherCreature.getId());
    }

    @Test
    void opponentMayChooseItsOwnFlagbearerInstead() {
        addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent bearer = addCreatureReady(player2, new StandardBearer());
        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, bearer.getId());

        harness.assertNotOnBattlefield(player2, "Standard Bearer");
        harness.assertInHand(player2, "Standard Bearer");
        harness.assertOnBattlefield(player1, "Coalition Honor Guard");
    }

    @Test
    void protectionMakesHonorGuardAnIllegalTargetWithoutPreventingOtherTargets() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        harness.setHand(player1, List.of(new ShieldOfDutyAndReason()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, honorGuard.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, otherCreature.getId());

        harness.assertInHand(player1, "Angelfire Crusader");
        harness.assertNotOnBattlefield(player1, "Angelfire Crusader");
        harness.assertOnBattlefield(player1, "Coalition Honor Guard");
    }

    @Test
    void kickedSpellMayTargetHonorGuardOnlyForItsSecondEffect() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        harness.setHand(player2, List.of(new Jilt()));
        addKickedJiltMana();

        harness.castKickedInstantWithSacrifices(player2, 0, otherCreature.getId(),
                List.of(honorGuard.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Angelfire Crusader");
        harness.assertNotOnBattlefield(player1, "Angelfire Crusader");
        assertThat(honorGuard.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void kickedSpellDoesNotRequireEveryTargetToBeAFlagbearer() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        harness.setHand(player2, List.of(new Jilt()));
        addKickedJiltMana();

        harness.castKickedInstantWithSacrifices(player2, 0, honorGuard.getId(),
                List.of(otherCreature.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coalition Honor Guard");
        harness.assertNotOnBattlefield(player1, "Coalition Honor Guard");
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void targetSelectionAllowsNonFlagbearerFirstWhenLaterTargetCanBeFlagbearer() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        Jilt jilt = new Jilt();
        harness.setHand(player2, List.of(jilt));
        addKickedJiltMana();

        var firstTargets = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, jilt, player2.getId(), List.of(), null, true);

        assertThat(firstTargets.validPermanentIds()).contains(otherCreature.getId(), honorGuard.getId());

        var secondTargets = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, jilt, player2.getId(), List.of(otherCreature.getId()), null, true);

        assertThat(secondTargets.validPermanentIds()).containsExactly(honorGuard.getId());
    }

    @Test
    void kickedSpellCannotOmitEveryLegalFlagbearer() {
        addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent first = addCreatureReady(player1, new AngelfireCrusader());
        Permanent second = addCreatureReady(player2, new AngelfireCrusader());
        harness.setHand(player2, List.of(new Jilt()));
        addKickedJiltMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(player2, 0, first.getId(),
                List.of(second.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");
    }

    @Test
    void opponentMayActivateAnAbilityWithoutTargets() {
        addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent crusader = addCreatureReady(player2, new AngelfireCrusader());
        int crusaderIndex = gd.playerBattlefields.get(player2.getId()).indexOf(crusader);
        harness.addMana(player2, ManaColor.RED, 1);
        int originalPower = gqs.getEffectivePower(gd, crusader);

        harness.activateAbility(player2, crusaderIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(originalPower + 1);
    }

    private void addKickedJiltMana() {
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }
}
