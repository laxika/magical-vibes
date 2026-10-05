package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PurifyTheGrave.class, WalkingCorpse.class, BumpInTheNight.class})
class PurifyTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Casting exiles target card from opponent's graveyard")
    void exilesCardFromOpponentGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Casting exiles target card from own graveyard")
    void exilesCardFromOwnGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Can exile any card type, not just creatures")
    void exilesNonCreatureCard() {
        Card sorcery = new BumpInTheNight();
        harness.setGraveyard(player2, List.of(sorcery));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, sorcery.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Bump in the Night");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Bump in the Night"));
    }

    @Test
    @DisplayName("Spell goes to graveyard after normal cast")
    void spellGoesToGraveyardAfterNormalCast() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Purify the Grave");
    }

    @Test
    @DisplayName("Puts spell on stack as instant")
    void putsOnStackAsInstant() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Purify the Grave");
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Flashback exiles target card from graveyard")
    void flashbackExilesCard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesSpellAfterResolving() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Purify the Grave");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Purify the Grave"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack with flashback flag")
    void flashbackPutsOnStack() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Purify the Grave");
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Flashback pays flashback cost")
    void flashbackPaysFlashbackCost() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new PurifyTheGrave()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, creature.getId());

        harness.assertNotInGraveyard(player1, "Purify the Grave");
    }

    @Test
    @DisplayName("Fizzles if target card is removed from graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Remove target before resolution
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback spell is exiled when its only target becomes illegal")
    void flashbackExilesSpellWhenTargetRemoved() {
        Card target = new WalkingCorpse();
        Card purify = new PurifyTheGrave();
        harness.setGraveyard(player2, List.of(target));
        harness.setGraveyard(player1, List.of(purify));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Purify the Grave");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(purify);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Exiles only the chosen card when multiple graveyard cards are available")
    void leavesOtherGraveyardCardsUntouched() {
        Card target = new WalkingCorpse();
        Card untouched = new BumpInTheNight();
        harness.setGraveyard(player2, List.of(target, untouched));
        harness.setHand(player1, List.of(new PurifyTheGrave()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }
}
