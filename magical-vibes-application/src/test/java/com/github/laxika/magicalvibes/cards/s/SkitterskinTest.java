package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuinousPath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skitterskin.class, SnappingGnarlid.class, SludgeCrawler.class,
        CompleteDisregard.class, Forest.class, RuinousPath.class})
class SkitterskinTest extends BaseCardTest {

    @Test
    @DisplayName("Skitterskin cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new Skitterskin());

        Permanent attacker = addCreatureReady(player1, new SnappingGnarlid());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Regeneration requires another colorless creature")
    void regenerationRequiresAnotherColorlessCreature() {
        addCreatureReady(player1, new Skitterskin());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another colorless creature enables regeneration")
    void anotherColorlessCreatureEnablesRegeneration() {
        Permanent skitterskin = addCreatureReady(player1, new Skitterskin());
        addCreatureReady(player1, new Skitterskin());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skitterskin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A colored creature does not enable regeneration")
    void coloredCreatureDoesNotEnableRegeneration() {
        addCreatureReady(player1, new Skitterskin());
        addCreatureReady(player1, new SnappingGnarlid());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's colorless creature does not enable regeneration")
    void opponentsColorlessCreatureDoesNotEnableRegeneration() {
        addCreatureReady(player1, new Skitterskin());
        addCreatureReady(player2, new SludgeCrawler());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A colorless noncreature does not enable regeneration")
    void colorlessNoncreatureDoesNotEnableRegeneration() {
        addCreatureReady(player1, new Skitterskin());
        harness.addToBattlefield(player1, new Forest());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enabling creature is needed only when activating")
    void losingEnablingCreatureDoesNotStopResolution() {
        Permanent skitterskin = addCreatureReady(player1, new Skitterskin());
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        addManaForAbility();
        harness.setHand(player2, List.of(new CompleteDisregard()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, crawler.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sludge Crawler");
        assertThat(skitterskin.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(skitterskin.getRegenerationShield()).isEqualTo(1);
        addManaForAbility();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration prevents destruction and taps Skitterskin")
    void regenerationPreventsDestruction() {
        Permanent skitterskin = addCreatureReady(player1, new Skitterskin());
        addCreatureReady(player1, new SludgeCrawler());
        harness.setHand(player1, List.of(new RuinousPath()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, skitterskin.getId());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skitterskin.isTapped()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skitterskin");
        harness.assertNotInGraveyard(player1, "Skitterskin");
        assertThat(skitterskin.isTapped()).isTrue();
        assertThat(skitterskin.getRegenerationShield()).isZero();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
