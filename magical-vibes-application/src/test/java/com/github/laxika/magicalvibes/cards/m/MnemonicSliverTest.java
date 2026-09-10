package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.cards.w.WingedSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MnemonicSliver.class, WingedSliver.class, LightningElemental.class, Forest.class})
class MnemonicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers, including opposing ones, gain the draw ability")
    void grantsAbilityToAllSlivers() {
        Permanent mnemonicSliver = addCreatureReady(player1, new MnemonicSliver());
        Permanent ownSliver = addCreatureReady(player1, new WingedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, mnemonicSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Activating the granted ability sacrifices the Sliver and draws a card")
    void sacrificesSliverAndDraws() {
        addCreatureReady(player1, new MnemonicSliver());
        Permanent ownSliver = addCreatureReady(player1, new WingedSliver());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownSliver);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(ownSliver.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("An opposing Sliver can activate the granted ability and draws for its controller")
    void opposingSliverCanActivateAndDraw() {
        addCreatureReady(player1, new MnemonicSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingSliver);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSliver.getCard());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new MnemonicSliver());
        Permanent nonSliver = addCreatureReady(player1, new LightningElemental());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Mnemonic Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent mnemonicSliver = addCreatureReady(player1, new MnemonicSliver());
        Permanent sliver = addCreatureReady(player1, new WingedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(mnemonicSliver);

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
    }

    @Test
    @CardUsed(AmoeboidChangeling.class)
    @DisplayName("A Sliver that loses all creature types no longer keeps its own granted ability")
    void losingSliverTypeRemovesItsGrantedAbility() {
        Permanent mnemonicSliver = addCreatureReady(player1, new MnemonicSliver());
        Permanent otherSliver = addCreatureReady(player1, new WingedSliver());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        assertThat(gs.getEffectiveActivatedAbilities(gd, mnemonicSliver)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid),
                1,
                null,
                mnemonicSliver.getId());
        harness.passBothPriorities();

        assertThat(gs.getEffectiveActivatedAbilities(gd, mnemonicSliver)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).hasSize(1);
    }
}
