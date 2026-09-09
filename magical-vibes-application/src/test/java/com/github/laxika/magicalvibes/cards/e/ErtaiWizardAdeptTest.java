package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErtaiWizardAdept.class, RagingGoblin.class, Spellbook.class})
class ErtaiWizardAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell and taps itself")
    void countersTargetSpell() {
        Permanent ertai = addCreatureReady(player1, new ErtaiWizardAdept());

        RagingGoblin goblin = new RagingGoblin();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, goblin, "{R}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Ertai, Wizard Adept");
        assertThat(ertai.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ErtaiWizardAdept());

        RagingGoblin goblin = new RagingGoblin();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, goblin, "{R}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a noncreature spell")
    void countersNoncreatureSpell() {
        Permanent ertai = addCreatureReady(player1, new ErtaiWizardAdept());
        Spellbook spellbook = new Spellbook();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spellbook, "{0}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, spellbook.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(ertai.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent ertai = addCreatureReady(player1, new ErtaiWizardAdept());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefield(player2, new RagingGoblin());

        assertThatThrownBy(() -> harness.activateAbility(
                        player1, 0, null, harness.getPermanentId(player2, "Raging Goblin")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent ertai = addCreatureReady(player1, new ErtaiWizardAdept());
        ertai.tap();

        RagingGoblin goblin = new RagingGoblin();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, goblin, "{R}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
