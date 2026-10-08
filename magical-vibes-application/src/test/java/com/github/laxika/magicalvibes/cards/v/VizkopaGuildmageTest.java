package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VizkopaGuildmage.class, AngelOfMercy.class, GrizzlyBears.class, Plains.class, Unsummon.class})
class VizkopaGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability grants lifelink to the target creature")
    void grantsLifelink() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        int lifeBefore = gd.getLife(player1.getId());
        attackWith(bears);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("First ability cannot target a player")
    void rejectsPlayerTarget() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent guildmage = gd.playerBattlefields.get(player1.getId()).getFirst();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Second ability drains each opponent for the life gained afterwards")
    void drainsOnLifeGain() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);

        activateDrainWatcher();

        gainThreeLife(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - 3);
    }

    @Test
    @DisplayName("Second ability ignores life gained by an opponent")
    void ignoresOpponentLifeGain() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);

        activateDrainWatcher();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainThreeLife(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20 + 3);
    }

    @Test
    @DisplayName("Two activations of the second ability drain twice per life-gain event")
    void activationsStack() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);
        addAbilityMana(player1);

        activateDrainWatcher();
        activateDrainWatcher();

        gainThreeLife(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20 - 6);
    }

    @Test
    @DisplayName("Second ability stops draining after the turn ends")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);

        activateDrainWatcher();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainThreeLife(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("First ability cannot target a noncreature permanent")
    void rejectsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        harness.addToBattlefield(player2, new Plains());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First ability can grant lifelink to an opponent's creature")
    void grantsLifelinkToOpponentCreature() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Granted lifelink expires at cleanup")
    void lifelinkExpiresAtCleanup() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The delayed ability triggers for every life-gain event this turn")
    void drainsForMultipleLifeGainEvents() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        addAbilityMana(player1);
        activateDrainWatcher();

        gainThreeLife(player1);
        gainThreeLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Life gained before the second ability resolves does not cause life loss")
    void ignoresEarlierLifeGain() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        gainThreeLife(player1);
        addAbilityMana(player1);

        activateDrainWatcher();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        gainThreeLife(player1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The delayed ability survives Vizkopa Guildmage leaving the battlefield")
    void drainsAfterSourceLeaves() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent guildmage = gd.playerBattlefields.get(player1.getId()).getFirst();
        addAbilityMana(player1);
        activateDrainWatcher();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, guildmage.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        gainThreeLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The second ability still resolves after its source leaves in response")
    void registersDrainAfterSourceLeavesInResponse() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent guildmage = gd.playerBattlefields.get(player1.getId()).getFirst();
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, guildmage.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        gainThreeLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The first ability does not grant lifelink to a target that left the battlefield")
    void targetLeavingInResponsePreventsLifelinkGrant() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, 0, null, bears.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returnedBears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink from the first ability triggers life loss from the second ability")
    void abilitiesWorkTogether() {
        harness.addToBattlefield(player1, new VizkopaGuildmage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana(player1);
        addAbilityMana(player1);
        activateDrainWatcher();
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        attackWith(bears);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void activateDrainWatcher() {
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    /** Casts Angel of Mercy (ETB: gain 3 life) and resolves everything it puts on the stack. */
    private void gainThreeLife(Player player) {
        harness.setHand(player, List.of(new AngelOfMercy()));
        harness.addMana(player, ManaColor.WHITE, 5);
        harness.clearPriorityPassed();
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }

    private void attackWith(Permanent creature) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(player1, List.of(index));
        resolveCombat(player1);
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
