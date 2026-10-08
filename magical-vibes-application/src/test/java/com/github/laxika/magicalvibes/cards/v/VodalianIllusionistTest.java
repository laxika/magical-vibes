package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.e.EmpyrialArmor;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianIllusionist.class, BenalishInfantry.class, WindingCanyons.class, EmpyrialArmor.class})
class VodalianIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("Ability phases out the targeted creature and taps the Illusionist")
    void phasesOutTargetCreature() {
        Permanent illusionist = addCreatureReady(player1, new VodalianIllusionist());
        Permanent creature = addCreatureReady(player2, new BenalishInfantry());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(illusionist.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), java.util.List.of()))
                .doesNotContain(creature);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("A phased-out creature phases in during its controller's next untap step")
    void phasedOutCreaturePhasesBackIn() {
        addCreatureReady(player1, new VodalianIllusionist());
        Permanent creature = addCreatureReady(player2, new BenalishInfantry());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Targeting a land is rejected")
    void cannotTargetLand() {
        addCreatureReady(player1, new VodalianIllusionist());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot be activated while the Illusionist is tapped")
    void cannotActivateWhileTapped() {
        Permanent illusionist = addCreatureReady(player1, new VodalianIllusionist());
        illusionist.tap();
        Permanent creature = addCreatureReady(player2, new BenalishInfantry());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), java.util.List.of()))
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("The ability can target a creature you control")
    void canTargetOwnCreature() {
        Permanent illusionist = addCreatureReady(player1, new VodalianIllusionist());
        Permanent creature = addCreatureReady(player1, new BenalishInfantry());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(illusionist);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The ability requires two blue mana")
    void requiresTwoBlueMana() {
        Permanent illusionist = addCreatureReady(player1, new VodalianIllusionist());
        Permanent creature = addCreatureReady(player2, new BenalishInfantry());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(illusionist.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), java.util.List.of()))
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("The Illusionist can phase itself out and untaps when it phases back in")
    void canPhaseItselfOut() {
        Permanent illusionist = addCreatureReady(player1, new VodalianIllusionist());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, illusionist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(illusionist);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(illusionist);
        assertThat(illusionist.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(illusionist);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(illusionist);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(illusionist);
        assertThat(illusionist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the ability's tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent illusionist = harness.addToBattlefieldAndReturn(player1, new VodalianIllusionist());
        Permanent creature = addCreatureReady(player2, new BenalishInfantry());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(illusionist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("An Aura controlled by another player phases in with its creature, still attached")
    void attachedAuraReturnsWithCreature() {
        addCreatureReady(player1, new VodalianIllusionist());
        Permanent creature = addCreatureReady(player2, new BenalishInfantry());
        harness.setHand(player1, java.util.List.of(new EmpyrialArmor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Empyrial Armor");
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player1, "Empyrial Armor");
        harness.assertNotInGraveyard(player2, "Benalish Infantry");

        harness.performUntapStep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).doesNotContain(creature);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }
}
