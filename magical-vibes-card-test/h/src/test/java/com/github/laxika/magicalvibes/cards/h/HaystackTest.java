package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Haystack.class, GrizzlyBears.class, Ornithopter.class, SwiftfootBoots.class})
class HaystackTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T}: phases out a target creature you control")
    void phasesOutTargetCreatureYouControl() {
        Permanent haystack = harness.addToBattlefieldAndReturn(player1, new Haystack());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(haystack), 0, null, creature.getId());

        assertThat(haystack.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Phased-out creature returns during its controller's next untap step")
    void phasesInOnNextUntapStep() {
        Permanent haystack = harness.addToBattlefieldAndReturn(player1, new Haystack());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(haystack), 0, null, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent haystack = harness.addToBattlefieldAndReturn(player1, new Haystack());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(haystack), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(haystack.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attached equipment phases out and returns with its host, even under another controller")
    void equipmentReturnsWithHostRatherThanItsOwnControllersUntap() {
        Permanent haystack = harness.addToBattlefieldAndReturn(player1, new Haystack());
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new SwiftfootBoots());
        equipment.setAttachedTo(creature.getId());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(haystack), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(equipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(equipment);

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(equipment);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature artifact you control")
    void cannotTargetNoncreatureArtifact() {
        Permanent haystack = harness.addToBattlefieldAndReturn(player1, new Haystack());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(haystack), 0, null, haystack.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(haystack.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating requires two mana and does not tap the source if payment fails")
    void cannotActivateWithoutEnoughMana() {
        Permanent haystack = harness.addToBattlefieldAndReturn(player1, new Haystack());
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(haystack), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(haystack.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
