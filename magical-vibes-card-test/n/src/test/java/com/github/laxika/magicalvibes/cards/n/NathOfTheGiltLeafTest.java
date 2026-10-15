package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.m.Mournwhelk;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NathOfTheGiltLeaf.class, WoodlandChangeling.class, Mournwhelk.class, Lignify.class})
class NathOfTheGiltLeafTest extends BaseCardTest {

    private long elfWarriorTokens(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR))
                .count();
    }

    @Test
    @DisplayName("Upkeep trigger only offers opponents as valid targets")
    void upkeepTargetFilterExcludesController() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new WoodlandChangeling()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Accepting the discard makes the opponent discard at random and creates an Elf Warrior token")
    void discardTriggersTokenCreation() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new WoodlandChangeling(), new WoodlandChangeling()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId()); // target opponent
        harness.passBothPriorities(); // resolve upkeep trigger â†’ may prompt for discard
        harness.handleMayAbilityChosen(player1, true); // opponent discards at random â†’ token trigger on stack
        harness.passBothPriorities(); // resolve token trigger â†’ may prompt for token
        harness.handleMayAbilityChosen(player1, true); // create the Elf Warrior token
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gameLogContains("at random")).isTrue();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(1)
                .allMatch(p -> p.getCard().getPower() == 1 && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.ELF)
                        && p.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }

    @Test
    @DisplayName("Declining the token trigger leaves the discard in place but no token")
    void discardWithoutTokenWhenDeclined() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new WoodlandChangeling(), new WoodlandChangeling()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // discard â†’ token trigger on stack
        harness.passBothPriorities(); // resolve token trigger â†’ may prompt
        harness.handleMayAbilityChosen(player1, false); // decline token
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(elfWarriorTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Declining the discard leaves the opponent's hand intact and makes no token")
    void decliningDiscardDoesNothing() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new WoodlandChangeling(), new WoodlandChangeling()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline discard
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(elfWarriorTokens(player1)).isZero();
    }

    @Test
    @DisplayName("An empty opposing hand produces no discard trigger or token")
    void emptyHandProducesNoToken() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(elfWarriorTokens(player1)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Another card causing two opposing discards creates two independently optional tokens")
    void otherCardDiscardTriggersOncePerCard() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new WoodlandChangeling(), new WoodlandChangeling()));
        harness.setHand(player1, List.of(new Mournwhelk()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(elfWarriorTokens(player1)).isEqualTo(1);
        assertThat(elfWarriorTokens(player2)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller discarding cards does not trigger Nath")
    void controllerDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player1, List.of(new Mournwhelk(), new WoodlandChangeling(), new WoodlandChangeling()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(elfWarriorTokens(player1)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Nath enchanted by Lignify does not trigger when an opponent discards")
    void losingAbilitiesStopsDiscardTrigger() {
        var nath = harness.addToBattlefieldAndReturn(player1, new NathOfTheGiltLeaf());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, nath.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Mournwhelk()));
        harness.setHand(player2, List.of(new WoodlandChangeling(), new WoodlandChangeling()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(elfWarriorTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new NathOfTheGiltLeaf());
        harness.setHand(player2, List.of(new WoodlandChangeling(), new WoodlandChangeling()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(elfWarriorTokens(player1)).isZero();
    }
}
