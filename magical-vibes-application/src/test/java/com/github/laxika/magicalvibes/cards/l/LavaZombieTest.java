package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LavaZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Entering prompts to return a black or red creature you control")
    void enteringPromptsForBlackOrRedCreature() {
        addCreatureReady(player1, new BlackCat());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player1, new GrizzlyBears());
        UUID blackCatId = harness.getPermanentId(player1, "Black Cat");
        UUID ragingGoblinId = harness.getPermanentId(player1, "Raging Goblin");
        UUID grizzlyBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, java.util.List.of(new LavaZombie()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        UUID lavaZombieId = harness.getPermanentId(player1, "Lava Zombie");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(blackCatId, ragingGoblinId, lavaZombieId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(grizzlyBearsId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen black or red creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        addCreatureReady(player1, new BlackCat());
        addCreatureReady(player1, new GrizzlyBears());
        UUID blackCatId = harness.getPermanentId(player1, "Black Cat");

        harness.setHand(player1, java.util.List.of(new LavaZombie()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, blackCatId);

        harness.assertInHand(player1, "Black Cat");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Lava Zombie");
    }

    @Test
    @DisplayName("The activated ability gives Lava Zombie +1/+0 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent lavaZombie = addCreatureReady(player1, new LavaZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lavaZombie.getPowerModifier()).isEqualTo(1);
        assertThat(lavaZombie.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void activatedAbilityBoostExpires() {
        Permanent lavaZombie = addCreatureReady(player1, new LavaZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lavaZombie.getPowerModifier()).isZero();
        assertThat(lavaZombie.getToughnessModifier()).isZero();
    }
}
