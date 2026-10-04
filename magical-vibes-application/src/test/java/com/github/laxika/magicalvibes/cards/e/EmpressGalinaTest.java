package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmpressGalina.class, CaptainSisay.class, DreamThrush.class})
class EmpressGalinaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of target legendary permanent")
    void gainsControlOfLegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonlegendary permanent")
    void cannotTargetNonlegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreamThrush());

        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, empress), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary permanent");
    }

    @Test
    @DisplayName("Control persists after Empress Galina leaves the battlefield")
    void controlPersistsAfterSourceLeaves() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, empress);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Empress Galina");
    }

    @Test
    @DisplayName("Ability resolves even if Empress Galina leaves before resolution")
    void abilityResolvesWithoutSource() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, empress);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Can target an already-controlled legendary permanent without untapping it")
    void canTargetOwnTappedLegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Permanent target = addCreatureReady(player1, new CaptainSisay());
        target.tap();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        assertThat(empress.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("A target that leaves and returns is not affected by the old ability")
    void doesNotGainControlOfReturnedTarget() {
        Permanent empress = addEmpressGalina();
        CaptainSisay sisay = new CaptainSisay();
        Permanent target = harness.addToBattlefieldAndReturn(player2, sisay);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        gd.playerHands.get(player2.getId()).remove(sisay);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, sisay);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the two blue mana cost with one blue and one colorless mana")
    void requiresTwoBlueMana() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, empress), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(empress.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Gaining control does not untap the target or grant haste, and untapping Galina does not end control")
    void controlDoesNotGrantUntapOrHasteAndSurvivesSourceUntapping() {
        Permanent empress = addEmpressGalina();
        Permanent target = addCreatureReady(player2, new CaptainSisay());
        target.tap();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.isSummoningSick()).isTrue();
        advanceToUpkeep(player1);

        assertThat(empress.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Summoning-sick Empress Galina cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent empress = harness.addToBattlefieldAndReturn(player1, new EmpressGalina());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, empress), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(empress.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addEmpressGalina() {
        return addCreatureReady(player1, new EmpressGalina());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
