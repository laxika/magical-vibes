package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GavonyIronwright;
import com.github.laxika.magicalvibes.cards.h.HighbornGhoul;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathsCaress.class, GavonyIronwright.class, HighbornGhoul.class, MaskwoodNexus.class})
class DeathsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Death's Caress targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HighbornGhoul());

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("A creature that was a Human only through Maskwood Nexus still counts as a Human once it is destroyed")
    void humanThroughMaskwoodNexusGainsLife() {
        harness.addToBattlefield(player2, new MaskwoodNexus());
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new HighbornGhoul());

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, ghoul.getId());

        harness.assertInGraveyard(player2, "Highborn Ghoul");
        // Highborn Ghoul is a 2/1 Zombie, every creature type through Maskwood Nexus as it died (CR 608.2h).
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Destroying a Human gains life equal to its toughness")
    void destroyingHumanGainsLife() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, human.getId());

        harness.assertNotOnBattlefield(player2, "Gavony Ironwright");
        harness.assertInGraveyard(player2, "Gavony Ironwright");
        // Gavony Ironwright has toughness 4, so controller gains 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Destroying a non-Human creature grants no life")
    void destroyingNonHumanGrantsNoLife() {
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player2, new HighbornGhoul());

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, nonHuman.getId());

        harness.assertNotOnBattlefield(player2, "Highborn Ghoul");
        harness.assertInGraveyard(player2, "Highborn Ghoul");
        // Highborn Ghoul is not a Human, so no life is gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Death's Caress goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HighbornGhoul());

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Death's Caress");
    }

    @Test
    void indestructibleHumanStillGrantsLife() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());
        PermanentGrants.keywordUntilEndOfTurn(gd, human, Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, human.getId());

        harness.assertOnBattlefield(player2, "Gavony Ironwright");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void regeneratingHumanStillGrantsLife() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());
        gd.addRegenerationShields(human.getId(), 1);
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, human.getId());

        harness.assertOnBattlefield(player2, "Gavony Ironwright");
        assertThat(human.isTapped()).isTrue();
        assertThat(gd.regenerationShieldCount(human.getId())).isZero();
        harness.assertLife(player1, 24);
    }

    @Test
    void usesModifiedToughnessAtResolution() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, human.getId());
        PermanentGrants.setToughnessBoost(gd, human, 3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gavony Ironwright");
        harness.assertLife(player1, 27);
    }

    @Test
    void canDestroyYourOwnHumanAndGainLife() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, human.getId());

        harness.assertInGraveyard(player1, "Gavony Ironwright");
        harness.assertLife(player1, 24);
    }

    @Test
    void checksHumanSubtypeAtResolution() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, human.getId());
        grantSubtypeUntilEndOfTurn(human, CardSubtype.ZOMBIE, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gavony Ironwright");
        harness.assertLife(player1, 20);
    }

    @Test
    void creatureThatBecomesHumanGrantsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HighbornGhoul());
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, target.getId());
        grantSubtypeUntilEndOfTurn(target, CardSubtype.HUMAN, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Highborn Ghoul");
        harness.assertLife(player1, 21);
    }

    @Test
    void targetGainingHexproofPreventsDestructionAndLifeGain() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, human.getId());
        PermanentGrants.keywordUntilEndOfTurn(gd, human, Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gavony Ironwright");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Death's Caress");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, human.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No life is gained when the spell fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        // Death's Caress still goes to the graveyard
        harness.assertInGraveyard(player1, "Death's Caress");
    }
}
