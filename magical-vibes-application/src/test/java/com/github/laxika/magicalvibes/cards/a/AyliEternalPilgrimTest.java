package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeersLantern;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyliEternalPilgrim.class, GrizzlyBears.class, Forest.class, SeersLantern.class})
class AyliEternalPilgrimTest extends BaseCardTest {

    @Test
    void gainsLifeEqualToSacrificedCreatureToughness() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeAyliItself() {
        Permanent ayli = addAyliReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesTargetNonlandPermanentAtThirtyLife() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void cannotActivateExileAbilityBelowLifeThreshold() {
        Permanent ayli = addAyliReady(player1);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 29);
        addExileAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(victim);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void cannotTargetLandWithExileAbility() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new Forest());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateExileAbilityBelowCommanderLifeThreshold() {
        gd.format = DeckFormat.COMMANDER;
        Permanent ayli = addAyliReady(player1);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 49);
        harness.setLife(player2, 40);
        addExileAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(victim);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void exilesAtCommanderLifeThreshold() {
        gd.format = DeckFormat.COMMANDER;
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 50);
        harness.setLife(player2, 40);
        addExileAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void exileStillResolvesAfterLifeDropsBelowThreshold() {
        Permanent ayli = addAyliReady(player1);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(victim);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.setLife(player1, 20);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertLife(player1, 20);
    }

    @Test
    void gainsLifeUsingToughnessWithCountersBeforeSacrifice() {
        Permanent ayli = addAyliReady(player1);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    void choosesAnotherCreatureAndUsesItsModifiedToughness() {
        Permanent ayli = addAyliReady(player1);
        Permanent unchosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ayli, unchosen).doesNotContain(victim);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeAyliToExileAbility() {
        Permanent ayli = addAyliReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ayli);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void exilesNoncreatureArtifactYouControl() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeersLantern());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ayli).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
    }

    @Test
    void canGainLifeWhileAyliIsTappedAndSummoningSick() {
        Permanent ayli = harness.addToBattlefieldAndReturn(player1, new AyliEternalPilgrim());
        ayli.setSummoningSick(true);
        ayli.setTapped(true);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificingTheExileTargetMakesTheAbilityFailToResolve() {
        Permanent ayli = addAyliReady(player1);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, ayli), 1, null, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(victim.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ayli).doesNotContain(victim);
    }
    private Permanent addAyliReady(Player player) {
        return addCreatureReady(player, new AyliEternalPilgrim());
    }

    private void addExileAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
