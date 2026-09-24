package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokHoversail.class, AlphaMyr.class})
class NeurokHoversailTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying")
    void equippedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent hoversail = addHoversailReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        hoversail.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses flying when Neurok Hoversail is unattached")
    void creatureLosesFlyingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent hoversail = addHoversailReady(player1);
        hoversail.setAttachedTo(creature.getId());

        hoversail.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Neurok Hoversail does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent otherCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent hoversail = addHoversailReady(player1);
        hoversail.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Neurok Hoversail to target creature")
    void equipAttachesToTargetCreature() {
        Permanent hoversail = addHoversailReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hoversail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Successful equip does not report that the ability fizzled")
    void successfulEquipDoesNotReportFizzle() {
        Permanent hoversail = addHoversailReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hoversail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void cannotEquipOpponentsCreature() {
        addHoversailReady(player1);
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreaturePermanent() {
        Permanent hoversail = addHoversailReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hoversail.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        addHoversailReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private Permanent addHoversailReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NeurokHoversail());
        perm.setSummoningSick(false);
        return perm;
    }
}
