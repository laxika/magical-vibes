package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedScroll.class, Counterspell.class, FightingDrake.class})
class CursedScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the controller to name a card")
    void resolvingPromptsController() {
        Permanent scroll = addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.ChooseNameRevealRandomHandCardDamageChoice.class);
        var context = (ChoiceContext.ChooseNameRevealRandomHandCardDamageChoice) interaction.context();
        assertThat(context.targetId()).isEqualTo(player2.getId());
        assertThat(context.sourcePermanentId()).isEqualTo(scroll.getId());
        assertThat(interaction.options()).contains("Cursed Scroll", "Counterspell");
        assertThat(scroll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealed card matching the chosen name deals 2 damage to the target player")
    void matchingRevealDeals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Counterspell");

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Revealed card with a different name deals no damage")
    void mismatchedRevealDealsNoDamage() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cursed Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Matching reveal can instead deal the 2 damage to a target creature")
    void matchingRevealDamagesTargetCreature() {
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        Permanent drake = addCreatureReady(player2, new FightingDrake());

        harness.activateAbility(player1, 0, null, drake.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(drake.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty hand reveals nothing and deals no damage")
    void emptyHandDealsNoDamage() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cursed Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The random reveal comes from the ability controller's hand")
    void revealUsesControllerHand() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));
        harness.setHand(player2, List.of(new CursedScroll()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cursed Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal target makes the ability fizzle before the name choice")
    void illegalTargetFizzlesBeforeNameChoice() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));
        Permanent drake = addCreatureReady(player2, new FightingDrake());

        harness.activateAbility(player1, 0, null, drake.getId());
        gd.playerBattlefields.get(player2.getId()).remove(drake);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyScroll(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CursedScroll());
        perm.setSummoningSick(false);
        return perm;
    }
}
