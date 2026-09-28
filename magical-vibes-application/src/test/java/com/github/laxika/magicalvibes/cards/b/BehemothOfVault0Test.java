package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BehemothOfVault0.class, DoomBlade.class, Forest.class, GrizzlyBears.class})
class BehemothOfVault0Test extends BaseCardTest {

    @Test
    @DisplayName("Enters with four energy counters")
    void entersWithFourEnergy() {
        Permanent behemoth = castBehemoth();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(behemoth);
    }

    @Test
    @DisplayName("When it dies, paying the target's mana value destroys a nonland permanent")
    void deathTriggerPaysTargetManaValueAndDestroysTarget() {
        Permanent behemoth = castBehemoth();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyBehemoth(behemoth);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the death trigger does not pay energy or destroy the target")
    void decliningDeathTriggerDoesNothing() {
        Permanent behemoth = castBehemoth();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyBehemoth(behemoth);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot pay the death trigger when energy is below the target's mana value")
    void insufficientEnergyDoesNotDestroyTarget() {
        Permanent behemoth = castBehemoth();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        destroyBehemoth(behemoth);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The death trigger only offers nonland permanents as targets")
    void deathTriggerRejectsLandTarget() {
        Permanent behemoth = castBehemoth();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyBehemoth(behemoth);

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(forest.getId());
        assertThat(choice.validIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castBehemoth() {
        harness.setHand(player1, List.of(new BehemothOfVault0()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Behemoth of Vault 0");
    }

    private void destroyBehemoth(Permanent behemoth) {
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, behemoth.getId());
        harness.passBothPriorities();
    }
}
