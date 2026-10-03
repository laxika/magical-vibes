package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakDown.class, Forest.class, GildedLotus.class, GloriousAnthem.class, GrizzlyBears.class})
class BreakDownTest extends BaseCardTest {

    @Test
    void destroysArtifactAndCreatesJunk() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());

        cast(artifact.getId());

        harness.assertNotOnBattlefield(player2, "Gilded Lotus");
        Permanent junk = findPermanent(player1, "Junk");
        assertThat(junk.getCard().getSubtypes()).containsExactly(CardSubtype.JUNK);
    }

    @Test
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void junkExilesTopCardWhenSacrificed() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());

        Permanent junk = findPermanent(player1, "Junk");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(junk.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
    }

    @Test
    void doesNotCreateJunkWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        prepareSpell();
        harness.castInstant(player1, 0, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Junk")).isZero();
        harness.assertInGraveyard(player1, "Break Down");
    }

    @Test
    void junkAllowsPlayingTheExiledLand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void junkRequiresPayingForTheExiledSpell() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void junkDoesNotGrantAnAdditionalLandPlay() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
    }

    @Test
    void junkPlayPermissionExpiresAtEndOfTurn() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Forest(), new Forest()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void junkCannotBeActivatedOutsideAMainPhase() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void junkCannotBeActivatedWhileASpellIsOnTheStack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        prepareSpell();
        harness.castInstant(player1, 0, otherArtifact.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void tappedJunkCannotBeActivated() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());
        findPermanent(player1, "Junk").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void junkCanBeSacrificedWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        cast(artifact.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(countPermanents(player1, "Junk")).isZero();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void cast(java.util.UUID targetId) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BreakDown()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
