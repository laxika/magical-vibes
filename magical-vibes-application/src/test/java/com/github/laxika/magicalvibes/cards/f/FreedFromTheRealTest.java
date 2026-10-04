package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.cards.r.RendingVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreedFromTheReal.class, HandOfHonor.class, RendingVines.class})
class FreedFromTheRealTest extends BaseCardTest {

    @Test
    void tapAbilityStillResolvesAfterAuraIsDestroyed() {
        abilityStillResolvesAfterAuraIsDestroyed(0, false, true);
    }

    @Test
    void untapAbilityStillResolvesAfterAuraIsDestroyed() {
        abilityStillResolvesAfterAuraIsDestroyed(1, true, false);
    }

    private void abilityStillResolvesAfterAuraIsDestroyed(int abilityIndex, boolean initiallyTapped,
                                                         boolean expectedTapped) {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        if (initiallyTapped) {
            creature.tap();
        }
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new RendingVines(), new HandOfHonor(),
                new HandOfHonor(), new HandOfHonor()));
        harness.setLibrary(player2, List.of(new HandOfHonor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura),
                abilityIndex, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Freed from the Real");
        assertThat(creature.isTapped()).isEqualTo(initiallyTapped);

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isEqualTo(expectedTapped);
    }

    @Test
    void castsAndAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        harness.setHand(player1, List.of(new FreedFromTheReal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Freed from the Real").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    void tapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void untapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void onlyAffectsTheEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        Permanent otherCreature = addCreatureReady(player1, new HandOfHonor());
        otherCreature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLUE, 2);
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIndex, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isTrue();

        harness.activateAbility(player1, auraIndex, 1, null, null);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
    }

    @Test
    void affectsAnEnchantedCreatureControlledByAnotherPlayer() {
        Permanent creature = addCreatureReady(player2, new HandOfHonor());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FreedFromTheReal());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLUE, 2);
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIndex, 0, null, null);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        harness.activateAbility(player1, auraIndex, 1, null, null);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
    }
}
