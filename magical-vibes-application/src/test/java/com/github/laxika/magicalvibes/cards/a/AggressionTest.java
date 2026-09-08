package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aggression.class, BalduvianBears.class, GlacialWall.class, SnowCoveredPlains.class})
class AggressionTest extends BaseCardTest {

    private Permanent attach(Player auraController, Permanent host) {
        Permanent aura = new Permanent(new Aggression());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private Permanent addCreature(Player owner) {
        return addCreatureReady(owner, new BalduvianBears());
    }

    private void runEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Enchanted creature has first strike and trample")
    void grantsFirstStrikeAndTrample() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature is destroyed at its controller's end step if it didn't attack")
    void destroysNonAttacker() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Enchanted creature survives if it attacked this turn")
    void sparesAttacker() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveCombat();

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Moving Aggression after its trigger does not change the creature it destroys")
    void triggerKeepsOriginalEnchantedCreatureWhenAuraMoves() {
        Permanent originalCreature = addCreature(player1);
        Permanent newCreature = addCreature(player1);
        newCreature.setAttackedThisTurn(true);
        Permanent aura = attach(player1, originalCreature);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        aura.setAttachedTo(newCreature.getId());

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(originalCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(newCreature);
    }

    @Test
    @DisplayName("Trigger only fires on the enchanted creature's controller's end step")
    void doesNotFireOnOtherPlayersEndStep() {
        Permanent bears = addCreature(player2);
        attach(player1, bears);

        runEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);

        runEndStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot enchant a Wall")
    void cannotEnchantWall() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new GlacialWall());
        harness.setHand(player1, List.of(new Aggression()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        harness.setHand(player1, List.of(new Aggression()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enchant a non-Wall creature")
    void enchantsNonWallCreature() {
        Permanent bears = addCreature(player1);
        harness.setHand(player1, List.of(new Aggression()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Aggression)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }
}
