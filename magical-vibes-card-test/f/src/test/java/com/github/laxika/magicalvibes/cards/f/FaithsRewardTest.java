package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealerOfThePride;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaithsReward.class, DoomBlade.class, GrizzlyBears.class, Naturalize.class,
        ObsidianBattleAxe.class, Pacifism.class, HealerOfThePride.class})
class FaithsRewardTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void castFaithsReward() {
        harness.castFromHand(player1, new FaithsReward(), "{3}{W}");
        resolveUntilInputOrEmpty();
    }

    @Test
    @DisplayName("Returns a creature and an artifact that died this turn to the battlefield")
    void returnsPermanentsThatDiedThisTurn() {
        Card bears = new GrizzlyBears();
        Card axe = new ObsidianBattleAxe();
        harness.addToBattlefield(player1, bears);
        harness.addToBattlefield(player1, axe);

        harness.setHand(player1, List.of(new DoomBlade(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Obsidian Battle-Axe"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Obsidian Battle-Axe");

        castFaithsReward();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Obsidian Battle-Axe");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()) || c.getId().equals(axe.getId()));
    }

    @Test
    @DisplayName("Does not return cards that were already in the graveyard")
    void doesNotReturnCardsNotPutThereFromBattlefieldThisTurn() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castFaithsReward();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not return a nonpermanent card put into the graveyard this turn")
    void doesNotReturnInstantCard() {
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        castFaithsReward();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doom Blade"));
    }

    @Test
    void returnsAuraAttachedToAnExistingCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new HealerOfThePride());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Pacifism"));

        castFaithsReward();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, creature.getId());
            resolveUntilInputOrEmpty();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pacifism")
                        && creature.getId().equals(p.getAttachedTo()));
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    void returningCreaturesSeeEachOthersEntryRegardlessOfGraveyardOrder() {
        harness.addToBattlefield(player1, new HealerOfThePride());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Healer of the Pride"));
        harness.setLife(player1, 20);

        castFaithsReward();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Healer of the Pride");
        harness.assertLife(player1, 22);
    }

    @Test
    void doesNotReturnOpponentsPermanentsThatDiedThisTurn() {
        harness.addToBattlefield(player2, new HealerOfThePride());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Healer of the Pride"));

        castFaithsReward();

        harness.assertInGraveyard(player2, "Healer of the Pride");
        harness.assertNotOnBattlefield(player1, "Healer of the Pride");
        harness.assertNotOnBattlefield(player2, "Healer of the Pride");
    }
}
