package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.c.ChokingTethers;
import com.github.laxika.magicalvibes.cards.c.CrownOfAscension;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.r.RiptideShapeshifter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinPiledriver.class, GoblinSledder.class, ElvishWarrior.class,
        AphettoAlchemist.class, RiptideShapeshifter.class, ChokingTethers.class,
        CrownOfAscension.class, Shock.class})
class GoblinPiledriverTest extends BaseCardTest {

    private Permanent addPiledriver(Player player) {
        return addCreatureReady(player, new GoblinPiledriver());
    }

    private Permanent addGoblin(Player player) {
        return addCreatureReady(player, new GoblinSledder());
    }

    @Test
    @DisplayName("Gets +2/+0 for each other attacking Goblin")
    void boostScalesWithOtherAttackingGoblins() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);
        addGoblin(player1);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(4);
        assertThat(piledriver.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking alone gives no boost (itself is not an 'other' Goblin)")
    void noBoostWhenAttackingAlone() {
        Permanent piledriver = addPiledriver(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-Goblin attackers do not increase the boost")
    void nonGoblinAttackersNotCounted() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);
        addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Goblin that stays home does not increase the boost")
    void nonAttackingGoblinNotCounted() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent piledriver = addPiledriver(player1);
        addGoblin(player1);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(piledriver.getPowerModifier()).isEqualTo(2);

        piledriver.setAttacking(false);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(piledriver.getPowerModifier()).isZero();
        assertThat(piledriver.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A blue creature cannot block Goblin Piledriver")
    void cannotBeBlockedByBlueCreature() {
        addPiledriver(player1);
        addCreatureReady(player2, new AphettoAlchemist());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A red creature can block Goblin Piledriver")
    void canBeBlockedByRedCreature() {
        addPiledriver(player1);
        Permanent blocker = addGoblin(player2);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage from a blue creature is prevented")
    void combatDamageFromBlueCreatureIsPrevented() {
        addCreatureReady(player1, new RiptideShapeshifter());
        Permanent piledriver = addPiledriver(player2);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Goblin Piledriver");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(piledriver.getCard());
    }

    @Test
    @DisplayName("Cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent piledriver = addPiledriver(player2);

        harness.setHand(player1, List.of(new ChokingTethers()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, piledriver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Cannot be enchanted by a blue Aura")
    void cannotBeEnchantedByBlueAura() {
        Permanent piledriver = addPiledriver(player2);

        harness.setHand(player1, List.of(new CrownOfAscension()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, piledriver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent piledriver = addPiledriver(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, piledriver.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }
}
