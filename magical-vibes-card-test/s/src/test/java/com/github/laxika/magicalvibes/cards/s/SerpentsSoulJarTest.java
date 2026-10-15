package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentsSoulJar.class, ElvishWarrior.class, FeralShadow.class,
        SavageTwister.class, GrizzlyBears.class, Shock.class})
class SerpentsSoulJarTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nontoken Elf that dies, but not another creature")
    void exilesDyingElfWithTheJar() {
        UUID jarId = addJar();
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new FeralShadow());

        castSavageTwister(3);

        assertThat(gd.getCardsExiledByPermanent(jarId))
                .extracting(Card::getName).containsExactly("Elvish Warrior");
        harness.assertNotInGraveyard(player1, "Elvish Warrior");
        harness.assertInGraveyard(player1, "Feral Shadow");
    }

    @Test
    @DisplayName("Pays life and allows one normal-cost creature cast from the jar until end of turn")
    void castsOneExiledCreature() {
        Permanent jar = addReadyJar();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        gd.addToExile(player1.getId(), firstCreature, jar.getId());
        gd.addToExile(player1.getId(), secondCreature, jar.getId());

        activateJar(jar);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(jar.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, firstCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, secondCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow noncreature cards or casts after the turn ends")
    void filtersAndExpiresCastPermission() {
        Permanent jar = addReadyJar();
        Card creature = new GrizzlyBears();
        Card nonCreature = new Shock();
        gd.addToExile(player1.getId(), creature, jar.getId());
        gd.addToExile(player1.getId(), nonCreature, jar.getId());

        activateJar(jar);

        assertThatThrownBy(() -> harness.castFromExile(player1, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID addJar() {
        harness.addToBattlefield(player1, new SerpentsSoulJar());
        return harness.getPermanentId(player1, "Serpent's Soul-Jar");
    }

    private Permanent addReadyJar() {
        Permanent jar = new Permanent(new SerpentsSoulJar());
        jar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(jar);
        return jar;
    }

    private void activateJar(Permanent jar) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jar), null, null);
        harness.passBothPriorities();
    }

    private void castSavageTwister(int xValue) {
        harness.setHand(player1, List.of(new SavageTwister()));
        harness.addMana(player1, ManaColor.RED, xValue + 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
