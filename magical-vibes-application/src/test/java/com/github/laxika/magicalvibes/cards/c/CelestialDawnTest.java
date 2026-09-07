package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Sirocco;
import com.github.laxika.magicalvibes.cards.s.StalkingTiger;
import com.github.laxika.magicalvibes.cards.u.UnyaroGriffin;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        CelestialDawn.class,
        Forest.class,
        Mountain.class,
        StalkingTiger.class,
        ViashinoWarrior.class,
        UnyaroGriffin.class,
        Incinerate.class,
        Sirocco.class,
        Dissipate.class
})
class CelestialDawnTest extends BaseCardTest {

    @Test
    @DisplayName("A Forest you control taps for white instead of green")
    void ownForestProducesWhite() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CelestialDawn());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An opponent's Mountain is unaffected — still taps for red")
    void opponentLandUnaffected() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.forceActivePlayer(player2);

        gs.tapPermanent(gd, player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("A green creature you control becomes white")
    void ownCreatureBecomesWhite() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new StalkingTiger());
        harness.addToBattlefield(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveColors(gd, tiger)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("An opponent's green creature keeps its color")
    void opponentCreatureKeepsColor() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player2, new StalkingTiger());
        harness.addToBattlefield(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveColors(gd, tiger)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A land you control keeps its color identity — only nonland permanents turn white")
    void landIsNotColored() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("White mana pays a red pip — Viashino Warrior castable off {W}{W}{W}{W}")
    void whitePaysColoredPipsOfAnyColor() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.setHand(player1, List.of(new ViashinoWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Viashino Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Non-white mana can only pay generic — {R}{R}{R}{R} can't cast Viashino Warrior")
    void otherManaOnlyPaysGeneric() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.setHand(player1, List.of(new ViashinoWarrior()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Viashino Warrior")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Non-white mana still pays the generic part alongside white for the pips")
    void otherManaPaysGenericPortion() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.setHand(player1, List.of(new ViashinoWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Viashino Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("A red spell you control is white on the stack")
    void ownSpellBecomesWhiteOnStack() {
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.addToBattlefield(player2, new UnyaroGriffin());
        Incinerate incinerate = new Incinerate();
        harness.setHand(player1, List.of(incinerate));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, incinerate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red instant or sorcery spell");
    }

    @Test
    @DisplayName("A blue instant card you own in hand is white")
    void ownNonlandCardInHandBecomesWhite() {
        harness.addToBattlefield(player1, new CelestialDawn());
        Dissipate dissipate = new Dissipate();
        harness.setHand(player1, List.of(dissipate));
        harness.setLife(player1, 3);
        harness.setHand(player2, List.of(new Sirocco()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dissipate);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
