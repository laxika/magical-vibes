package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShikoAndNarsetUnified.class, DarkRitual.class, LightningBolt.class,
        Counterspell.class, HolyStrength.class})
class ShikoAndNarsetUnifiedTest extends BaseCardTest {

    @Test
    void drawsWhenTheSecondSpellDoesNotTarget() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningNewTargetsStillCopiesAndDoesNotDraw() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 11);
    }

    @Test
    void choosingNewTargetsChangesOnlyTheCopy() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyTheSecondSpellDraws() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsSpellsDoNotTriggerFlurry() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void countsSpellsCastBeforeShikoAndNarsetEntered() {
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void triggersAgainOnTheOpponentsTurn() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.setLibrary(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsForASpellThatTargetsOnlyAnotherSpell() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new DarkRitual(), new Counterspell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        DarkRitual opponentsSpell = new DarkRitual();
        harness.setHand(player2, List.of(opponentsSpell));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, opponentsSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Dark Ritual");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesTheSecondSpellEvenAfterItIsCountered() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(new DarkRitual(), bolt));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt.getId());
        harness.assertInGraveyard(player1, "Lightning Bolt");

        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void copiesAnAuraAsAnAttachedToken() {
        var shiko = harness.addToBattlefieldAndReturn(player1, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new DarkRitual(), new HolyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.castEnchantment(player1, 0, shiko.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Holy Strength"))
                .hasSize(2)
                .allMatch(permanent -> shiko.getId().equals(permanent.getAttachedTo()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void copiesASpellTargetingAPermanentEvenWhenTheCopyKillsItsTarget() {
        harness.addToBattlefield(player1, new ShikoAndNarsetUnified());
        var target = harness.addToBattlefieldAndReturn(player2, new ShikoAndNarsetUnified());
        harness.setLibrary(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shiko and Narset, Unified");
        harness.assertInGraveyard(player2, "Shiko and Narset, Unified");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LightningBolt)
                .hasSize(2);
    }
}
