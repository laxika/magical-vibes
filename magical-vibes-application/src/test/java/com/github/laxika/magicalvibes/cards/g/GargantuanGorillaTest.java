package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GargantuanGorillaTest extends BaseCardTest {

    private Permanent gorilla(Player owner) {
        UUID id = harness.getPermanentId(owner, "Gargantuan Gorilla");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("No Forest to sacrifice: the Gorilla is sacrificed without a prompt and deals 7 damage to its controller")
    void noForestSacrificesItselfAndDealsSevenDamage() {
        harness.addToBattlefield(player1, new GargantuanGorilla());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Gargantuan Gorilla");
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Declining the sacrifice keeps the Forest but sacrifices the Gorilla for 7 damage")
    void decliningSacrificesTheGorilla() {
        harness.addToBattlefield(player1, new GargantuanGorilla());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Gargantuan Gorilla");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Sacrificing a nonsnow Forest saves the Gorilla but grants no trample")
    void plainForestSavesTheGorillaWithoutTrample() {
        harness.addToBattlefield(player1, new GargantuanGorilla());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, gorilla(player1), Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a snow Forest grants the Gorilla trample until end of turn")
    void snowForestGrantsTrample() {
        harness.addToBattlefield(player1, new GargantuanGorilla());
        harness.addToBattlefield(player1, new SnowCoveredForest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Snow-Covered Forest");
        assertThat(gqs.hasKeyword(gd, gorilla(player1), Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The trample from a snow Forest wears off at end of turn")
    void trampleWearsOff() {
        harness.addToBattlefield(player1, new GargantuanGorilla());
        harness.addToBattlefield(player1, new SnowCoveredForest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, gorilla(player1), Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("With several Forests the controller chooses which one to sacrifice")
    void chooseWhichForestToSacrifice() {
        harness.addToBattlefield(player1, new GargantuanGorilla());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        UUID snowForest = harness.getPermanentId(player1, "Snow-Covered Forest");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, snowForest);

        harness.assertNotOnBattlefield(player1, "Snow-Covered Forest");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, gorilla(player1), Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Fight: the 7/7 Gorilla kills a 3/3 and survives with marked damage")
    void fightKillsSmallerCreature() {
        Permanent gorilla = readyGorilla(player1);
        Permanent hillGiant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(hillGiant);

        harness.activateAbility(player1, 0, null, hillGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(hillGiant.getId()));
        assertThat(gorilla.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The fight ability can't target the Gorilla itself")
    void cannotFightItself() {
        Permanent gorilla = readyGorilla(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gorilla.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent readyGorilla(Player player) {
        Permanent perm = new Permanent(new GargantuanGorilla());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
