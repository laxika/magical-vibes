package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.cards.v.VirtueOfLoyalty;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchonsGlory.class, CandyTrail.class, BesottedKnight.class,
        HopefulVigil.class, VirtueOfLoyalty.class, RoostOfDrakes.class})
class ArchonsGloryTest extends BaseCardTest {

    @Test
    void withoutBargainBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());

        castArchonsGlory(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void canDeclineBargainWhenAnEligibleArtifactIsAvailable() {
        harness.addToBattlefield(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());

        castArchonsGlory(target.getId());

        harness.assertOnBattlefield(player1, "Candy Trail");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotBargainWithoutChoosingASacrifice() {
        harness.addToBattlefield(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Archon's Glory");
        harness.assertOnBattlefield(player1, "Candy Trail");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void withBargainSacrificesArtifactAndGrantsFlyingAndLifelink() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(sacrifice.getId()));
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void bargainKeywordsAndBoostWearOffAtEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void cannotBargainBySacrificingACreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void withBargainSacrificesEnchantment() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VirtueOfLoyalty());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.assertInGraveyard(player1, "Virtue of Loyalty");
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void withBargainSacrificesNonArtifactNonEnchantmentCreatureToken() {
        Permanent sacrifice = createKnightToken();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(sacrifice.getId()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void canSacrificeTargetTokenAndSpellDoesNotResolve() {
        Permanent target = createKnightToken();
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Archon's Glory");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBargainBySacrificingOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Candy Trail");
        harness.assertInHand(player1, "Archon's Glory");
    }

    @Test
    void bargainDoesNotTriggerAbilitiesForCastingKickedSpells() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }
    private Permanent createKnightToken() {
        harness.setHand(player1, List.of(new HopefulVigil()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void castArchonsGlory(UUID targetId) {
        harness.setHand(player1, List.of(new ArchonsGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
