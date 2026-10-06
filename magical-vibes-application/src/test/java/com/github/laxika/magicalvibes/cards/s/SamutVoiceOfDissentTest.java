package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamutVoiceOfDissent.class, DuneBeetle.class, Plains.class})
class SamutVoiceOfDissentTest extends BaseCardTest {


    @Test
    @DisplayName("Other creatures you control have haste")
    void grantsHasteToOtherCreatures() {
        harness.addToBattlefield(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player1, new DuneBeetle());

        Permanent bears = findPermanent(player1, "Dune Beetle");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to opponent's creatures")
    void doesNotGrantHasteToOpponentCreatures() {
        harness.addToBattlefield(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player2, new DuneBeetle());

        Permanent bears = findPermanent(player2, "Dune Beetle");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is removed when Samut leaves the battlefield")
    void hasteRemovedWhenSamutLeaves() {
        harness.addToBattlefield(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player1, new DuneBeetle());

        Permanent bears = findPermanent(player1, "Dune Beetle");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Samut, Voice of Dissent"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }


    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTargetCreature() {
        addCreatureReady(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player2, new DuneBeetle());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = findPermanent(player2, "Dune Beetle");
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the untap ability taps Samut as its cost")
    void activatingTapsSamut() {
        Permanent samut = addCreatureReady(player1, new SamutVoiceOfDissent());
        harness.addToBattlefield(player2, new DuneBeetle());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = findPermanent(player2, "Dune Beetle");

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(samut.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot untap itself — target must be another creature")
    void cannotTargetItself() {
        Permanent samut = addCreatureReady(player1, new SamutVoiceOfDissent());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, samut.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("Haste allows Samut to activate on the turn it enters")
    void activatesWhileSummoningSick() {
        Permanent samut = harness.addToBattlefieldAndReturn(player1, new SamutVoiceOfDissent());
        samut.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(samut.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new SamutVoiceOfDissent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untap ability resolves even after Samut leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent samut = addCreatureReady(player1, new SamutVoiceOfDissent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(samut);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting Samut during the opponent's upkeep")
    void castsDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SamutVoiceOfDissent()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Samut, Voice of Dissent")).isEqualTo(1);
    }

    @Test
    @DisplayName("Samut and another new creature can attack immediately; Samut stays untapped and deals double-strike damage")
    void hasteVigilanceAndDoubleStrikeWorkInCombat() {
        Permanent samut = harness.addToBattlefieldAndReturn(player1, new SamutVoiceOfDissent());
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        samut.setSummoningSick(true);
        beetle.setSummoningSick(true);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(samut.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }
}
