package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.g.GhituFire;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevouringStrossus.class, ArdentSoldier.class, GhituFire.class, Swamp.class})
class DevouringStrossusTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of upkeep, sacrifices a creature")
    void sacrificesCreatureAtBeginningOfUpkeep() {
        harness.addToBattlefield(player1, new DevouringStrossus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devouring Strossus");
        harness.assertInGraveyard(player1, "Devouring Strossus");
    }

    @Test
    @DisplayName("At upkeep, the controller chooses which creature to sacrifice")
    void controllerChoosesCreatureToSacrificeAtUpkeep() {
        Permanent strossus = harness.addToBattlefieldAndReturn(player1, new DevouringStrossus());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new ArdentSoldier());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(strossus.getId(), otherCreature.getId());

        harness.handlePermanentChosen(player1, otherCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strossus).doesNotContain(otherCreature);
    }

    @Test
    @DisplayName("At upkeep, sacrifices a creature rather than a land")
    void upkeepIgnoresNonCreaturePermanents() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent strossus = harness.addToBattlefieldAndReturn(player1, new DevouringStrossus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(swamp).doesNotContain(strossus);
    }

    @Test
    @DisplayName("Sacrificing a creature grants regeneration and survives lethal damage")
    void sacrificingCreatureRegeneratesAndSurvivesLethalDamage() {
        Permanent strossus = harness.addToBattlefieldAndReturn(player1, new DevouringStrossus());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new ArdentSoldier());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strossus).doesNotContain(otherCreature);
        assertThat(strossus.getRegenerationShield()).isEqualTo(1);

        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castAndResolveSorcery(player1, 0, 9, strossus.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strossus);
        assertThat(strossus.isTapped()).isTrue();
        assertThat(strossus.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The regeneration ability may sacrifice Devouring Strossus itself")
    void regenerationAbilityMaySacrificeItself() {
        Permanent strossus = harness.addToBattlefieldAndReturn(player1, new DevouringStrossus());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(strossus);
        harness.assertInGraveyard(player1, "Devouring Strossus");
    }
}
