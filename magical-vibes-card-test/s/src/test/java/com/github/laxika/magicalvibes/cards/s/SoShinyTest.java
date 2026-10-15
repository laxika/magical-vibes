package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LiquimetalTorque;
import com.github.laxika.magicalvibes.cards.n.NestedShambler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoShiny.class, NestedShambler.class, LiquimetalTorque.class})
class SoShinyTest extends BaseCardTest {

    @Test
    void tokenControlTapsEnchantedCreatureAndScriesTwo() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        addToken(player1);
        Card first = new NestedShambler();
        Card second = new SoShiny();
        Card third = new NestedShambler();
        harness.setLibrary(player1, List.of(first, second, third));
        castSoShiny(creature);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void withoutTokenControlEtbDoesNotTapOrScry() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        castSoShiny(creature);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enchantedCreatureDoesNotUntapDuringItsControllersUntapStep() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoShiny());
        aura.setAttachedTo(creature.getId());
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void opponentsTokenDoesNotEnableTheTrigger() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        addToken(player2);
        castSoShiny(creature);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void losingTheLastTokenBeforeResolutionPreventsBothEffects() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        Permanent token = addToken(player1);
        castSoShiny(creature);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(token);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void gainingATokenAfterEntryDoesNotCreateATrigger() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        castSoShiny(creature);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        addToken(player1);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void replacingTheTokenBeforeResolutionStillEnablesBothEffects() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        Permanent token = addToken(player1);
        harness.setLibrary(player1, List.of(new NestedShambler(), new SoShiny()));
        castSoShiny(creature);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(token);
        addToken(player1);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void alreadyTappedCreatureStillAllowsScry() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        creature.tap();
        addToken(player1);
        harness.setLibrary(player1, List.of(new NestedShambler(), new SoShiny()));
        castSoShiny(creature);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void emptyLibraryDoesNotPreventTapping() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        addToken(player1);
        harness.setLibrary(player1, List.of());
        castSoShiny(creature);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removingTheAuraAllowsTheCreatureToUntapAgain() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        castSoShiny(creature);
        resolveAllTriggers();
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        Permanent aura = findPermanent(player1, "So Shiny");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void noncreatureTokenAlsoEnablesBothEffects() {
        Permanent creature = addCreatureReady(player2, new NestedShambler());
        Card token = new LiquimetalTorque();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.setLibrary(player1, List.of(new NestedShambler(), new SoShiny()));
        castSoShiny(creature);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    private void castSoShiny(Permanent creature) {
        harness.setHand(player1, List.of(new SoShiny()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
    }

    private Permanent addToken(Player player) {
        Card token = new NestedShambler();
        token.setToken(true);
        return harness.addToBattlefieldAndReturn(player, token);
    }
}
