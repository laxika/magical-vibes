package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bonehoard.class, LeoninSkyhunter.class, DivineOffering.class, JinnieFayJetmirsSecond.class})
class BonehoardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bonehoard triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Bonehoard");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Need at least 1 creature in graveyard so Germ survives (0/0 + X/X where X >= 1)
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent bonehoard = findPermanent(player1, "Bonehoard");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(bonehoard.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Germ dies to SBAs when no creature cards in any graveyard (0/0 toughness)")
    void germDiesWithEmptyGraveyards() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        // Germ should be dead (0/0 toughness with no creatures in graveyards)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");

        // Bonehoard should remain on the battlefield unattached
        harness.assertOnBattlefield(player1, "Bonehoard");
    }

    @Test
    @DisplayName("Germ gets +X/+X from creature cards in controller's graveyard")
    void germGetsBoostFromControllerGraveyard() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put 2 creature cards in player1's graveyard
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 2/2 boost = 2/2
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts creature cards in all players' graveyards")
    void boostCountsAllGraveyards() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put 1 creature in player1's graveyard, 2 in player2's graveyard
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());
        gd.playerGraveyards.get(player2.getId()).add(new LeoninSkyhunter());
        gd.playerGraveyards.get(player2.getId()).add(new LeoninSkyhunter());

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 3/3 boost (1 + 2 creature cards) = 3/3
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost updates dynamically when creatures enter graveyards")
    void boostUpdatesDynamically() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Start with 1 creature so Germ survives
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // Initially 1/1 (one creature in graveyard)
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);

        // Add another creature to graveyard — now 2/2
        gd.playerGraveyards.get(player2.getId()).add(new LeoninSkyhunter());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);

        // Add a third — now 3/3
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipping Bonehoard to another creature applies the graveyard-based boost")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put 2 creatures in graveyards
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());
        gd.playerGraveyards.get(player2.getId()).add(new LeoninSkyhunter());

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        // Add a creature to equip to
        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        // Equip to skyhunter
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, skyhunter.getId());
        harness.passBothPriorities();

        Permanent bonehoard = findPermanent(player1, "Bonehoard");

        assertThat(bonehoard.getAttachedTo()).isEqualTo(skyhunter.getId());

        // Skyhunter: 2/2 base + 2/2 boost (tokens don't count as creature cards) = 4/4
        assertThat(gqs.getEffectivePower(gd, skyhunter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skyhunter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Germ dies when Bonehoard is moved to another creature (0/0 with no equipment)")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put a creature in graveyard so germ survives initially
        gd.playerGraveyards.get(player1.getId()).add(new LeoninSkyhunter());

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        // Equip to skyhunter
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, skyhunter.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness after losing equipment boost, SBA)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Noncreature cards in graveyards do not increase the bonus")
    void noncreatureCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(new LeoninSkyhunter(), new Bonehoard()));
        harness.setGraveyard(player2, List.of(new DivineOffering(), new Bonehoard()));
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus decreases when creature cards leave graveyards")
    void boostDecreasesWhenGraveyardsShrink() {
        harness.setGraveyard(player1, List.of(new LeoninSkyhunter()));
        harness.setGraveyard(player2, List.of(new LeoninSkyhunter()));
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Living weapon still resolves after Bonehoard is destroyed in response")
    void equipmentDestroyedBeforeLivingWeaponResolves() {
        harness.setGraveyard(player1, List.of(new LeoninSkyhunter()));
        harness.setHand(player1, List.of(new Bonehoard(), new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Bonehoard");
        harness.castAndResolveInstant(player1, 0, equipment.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bonehoard");
        harness.assertNotOnBattlefield(player1, "Bonehoard");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        harness.assertNotInGraveyard(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Living weapon attaches Bonehoard to the Cat created by Jinnie Fay")
    void livingWeaponAttachesToReplacementToken() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setGraveyard(player1, List.of(new LeoninSkyhunter()));
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Cat");

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(findPermanent(player1, "Bonehoard").getAttachedTo()).isEqualTo(cat.getId());
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining Jinnie Fay still attaches Bonehoard before the Germ can die")
    void livingWeaponAttachesWhenReplacementDeclined() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setGraveyard(player1, List.of(new LeoninSkyhunter()));
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Original tokens");

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(findPermanent(player1, "Bonehoard").getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }
}
