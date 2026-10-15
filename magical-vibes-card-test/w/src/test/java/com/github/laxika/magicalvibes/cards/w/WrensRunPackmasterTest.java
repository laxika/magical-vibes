package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.m.MirrorEntity;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrensRunPackmaster.class, AvianChangeling.class, MirrorEntity.class})
class WrensRunPackmasterTest extends BaseCardTest {

    private Permanent wolfOnBattlefield(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Champion ETB auto-sacrifices when no other Elf is controlled")
    void championAutoSacrificesWithoutElf() {
        harness.castFromHand(player1, new WrensRunPackmaster(), "{3}{G}");
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> no Elf -> sacrifice

        harness.assertNotOnBattlefield(player1, "Wren's Run Packmaster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Champion ETB prompts a choice when another Elf is controlled")
    void championPromptsChoiceWithElf() {
        harness.addToBattlefield(player1, new AvianChangeling()); // counts as an Elf
        harness.castFromHand(player1, new WrensRunPackmaster(), "{3}{G}");
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player1, "Wren's Run Packmaster");
    }

    @Test
    @DisplayName("Activated ability creates a 2/2 green Wolf token")
    void activatedAbilityCreatesWolfToken() {
        harness.addToBattlefield(player1, new WrensRunPackmaster());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the token-creation ability

        Permanent wolf = wolfOnBattlefield(player1.getId());
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolves the controller controls gain deathtouch, other creatures do not")
    void wolvesGainDeathtouch() {
        Permanent packmaster = harness.addToBattlefieldAndReturn(player1, new WrensRunPackmaster());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = wolfOnBattlefield(player1.getId());
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
        // The Packmaster itself is an Elf, not a Wolf.
        assertThat(gqs.hasKeyword(gd, packmaster, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Champion can be declined even when an Elf is available")
    void canSacrificeInsteadOfExilingElf() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.castFromHand(player1, new WrensRunPackmaster(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Wren's Run Packmaster");
        harness.assertNotOnBattlefield(player1, "Wren's Run Packmaster");
        harness.assertOnBattlefield(player1, "Avian Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Champion excludes Packmaster itself and opponents' Elves")
    void championOffersOnlyAnotherOwnElf() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        harness.addToBattlefield(player2, new AvianChangeling());
        harness.castFromHand(player1, new WrensRunPackmaster(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ownElf.getId());
    }

    @Test
    @DisplayName("Champion exiles the chosen Elf and returns it only after the leave trigger resolves")
    void championReturnsElfThroughLeaveTrigger() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        harness.castFromHand(player1, new WrensRunPackmaster(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.handlePermanentChosen(player1, elf.getId());
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Avian Changeling"));

        Permanent packmaster = findPermanent(player1, "Wren's Run Packmaster");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, packmaster));

        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avian Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's Wolves gain deathtouch and the grant ends when Packmaster leaves")
    void deathtouchScopeAndDuration() {
        Permanent packmaster = harness.addToBattlefieldAndReturn(player1, new WrensRunPackmaster());
        Permanent ownWolf = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        Permanent opponentWolf = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());

        assertThat(gqs.hasKeyword(gd, ownWolf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentWolf, Keyword.DEATHTOUCH)).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, packmaster));

        assertThat(gqs.hasKeyword(gd, ownWolf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Packmaster gains deathtouch when Mirror Entity makes it a Wolf")
    void packmasterAsWolfGainsDeathtouch() {
        Permanent packmaster = harness.addToBattlefieldAndReturn(player1, new WrensRunPackmaster());
        harness.addToBattlefield(player1, new MirrorEntity());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, packmaster, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The token ability resolves after Packmaster leaves and creates a Wolf without deathtouch")
    void tokenAbilitySurvivesSourceLeaving() {
        Permanent packmaster = harness.addToBattlefieldAndReturn(player1, new WrensRunPackmaster());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, packmaster));
        harness.passBothPriorities();

        Permanent wolf = wolfOnBattlefield(player1.getId());
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Elf cannot prevent the champion sacrifice")
    void opponentElfCannotBeChampioned() {
        harness.addToBattlefield(player2, new AvianChangeling());
        harness.castFromHand(player1, new WrensRunPackmaster(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wren's Run Packmaster");
        harness.assertNotOnBattlefield(player1, "Wren's Run Packmaster");
        harness.assertOnBattlefield(player2, "Avian Changeling");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The token ability can be activated repeatedly while Packmaster is tapped")
    void tappedPackmasterCanCreateMultipleWolves() {
        Permanent packmaster = harness.addToBattlefieldAndReturn(player1, new WrensRunPackmaster());
        packmaster.tap();
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Wolf"))
                .hasSize(2)
                .allSatisfy(wolf -> {
                    assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
                    assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
                });
    }
}
