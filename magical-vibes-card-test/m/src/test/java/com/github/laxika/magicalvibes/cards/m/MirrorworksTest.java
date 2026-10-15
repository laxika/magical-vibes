package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.cards.i.IndomitableArchangel;
import com.github.laxika.magicalvibes.cards.s.SphereOfTheSuns;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mirrorworks.class, GlintHawkIdol.class, SphereOfTheSuns.class, IndomitableArchangel.class})
class MirrorworksTest extends BaseCardTest {

    @Test
    @DisplayName("Casting another artifact triggers may-pay ability")
    void castingAnotherArtifactTriggersMayPayAbility() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, MayPayManaEffect on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack -> may prompt

        // Should be prompted with may ability to pay {2}
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Paying {2} creates a token copy of the entering artifact")
    void payingCreatesTokenCopy() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, MayPayManaEffect on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack -> may prompt

        // Accept and pay {2}; the copy effect resolves inline.
        harness.handleMayAbilityChosen(player1, true);

        // Should have Mirrorworks + original Glint Hawk Idol + token copy = 3 permanents
        long idolCount = countPermanents(player1, "Glint Hawk Idol");
        assertThat(idolCount).isEqualTo(2);

        // One of them should be a token
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining does not create a token")
    void decliningDoesNotCreateToken() {
        addMirrorworksReady(player1);
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, MayPayManaEffect on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack -> may prompt

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        // Should have only Mirrorworks + original Glint Hawk Idol
        long idolCount = countPermanents(player1, "Glint Hawk Idol");
        assertThat(idolCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay if not enough mana")
    void cannotPayIfNotEnoughMana() {
        addMirrorworksReady(player1);
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, MayPayManaEffect on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack -> may prompt

        // Try to accept but can't afford it
        harness.handleMayAbilityChosen(player1, true);

        // No token should be created
        long idolCount = countPermanents(player1, "Glint Hawk Idol");
        assertThat(idolCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy entering does not trigger Mirrorworks again (nontoken)")
    void tokenCopyDoesNotRetrigger() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, MayPayManaEffect on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack -> may prompt

        // Accept and pay {2}; the copy effect resolves inline.
        harness.handleMayAbilityChosen(player1, true);

        // The token entering should NOT trigger Mirrorworks again (nontoken restriction)
        // Check that we don't have another may ability prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on itself entering")
    void doesNotTriggerOnItself() {
        harness.castFromHand(player1, new Mirrorworks(), "{5}");
        harness.passBothPriorities(); // resolve Mirrorworks spell

        // Should not prompt for may ability (no other artifact with the trigger)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Token copy has same abilities as the original")
    @CardUsed({SphereOfTheSuns.class})
    void tokenCopyHasSameAbilities() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new GlintHawkIdol(), "{2}");
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, MayPayManaEffect on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack -> may prompt

        // Accept and pay {2}; the copy effect resolves inline.
        harness.handleMayAbilityChosen(player1, true);

        // Find the token copy
        Permanent tokenCopy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol") && p.getCard().isToken())
                .findFirst().orElseThrow();

        // The original Idol triggers when its token copy enters.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tokenCopy), null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, tokenCopy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tokenCopy)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new SphereOfTheSuns());
        assertThat(gd.stack).hasSize(3);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Sphere of the Suns")).isEqualTo(1);
    }

    @Test
    @CardUsed({Mirrorworks.class, SphereOfTheSuns.class})
    void copiesArtifactThatLeftBeforeResolution() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent sphere = harness.enterBattlefieldAndReturn(player1, new SphereOfTheSuns());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, sphere));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Sphere of the Suns"))
                .singleElement().satisfies(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
                });
    }

    @Test
    @CardUsed({Mirrorworks.class, SphereOfTheSuns.class})
    void copiedArtifactAppliesItsOwnEntryAbilities() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Permanent sphere = harness.enterBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 1);
        sphere.untap();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
                });
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({Mirrorworks.class, SphereOfTheSuns.class})
    void opponentsArtifactDoesNotTrigger() {
        addMirrorworksReady(player1);
        harness.enterBattlefieldAndReturn(player2, new SphereOfTheSuns());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({Mirrorworks.class, SphereOfTheSuns.class})
    void triggerResolvesAfterMirrorworksLeaves() {
        Permanent mirrorworks = addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.enterBattlefieldAndReturn(player1, new SphereOfTheSuns());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mirrorworks));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Sphere of the Suns")).isEqualTo(2);
    }

    @Test
    @CardUsed({Mirrorworks.class, SphereOfTheSuns.class, IndomitableArchangel.class})
    void copiesArtifactWithShroudWithoutTargetingIt() {
        addMirrorworksReady(player1);
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new SphereOfTheSuns());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.enterBattlefieldAndReturn(player1, new SphereOfTheSuns());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Sphere of the Suns")).isEqualTo(3);
    }

    private Permanent addMirrorworksReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mirrorworks());
    }
}
