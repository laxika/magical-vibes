package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UniteTheCoalition.class, GrizzlyBears.class, Shock.class, Spellbook.class, GloriousAnthem.class})
class UniteTheCoalitionTest extends BaseCardTest {

    @Test
    void resolvesAllFiveModes() {
        Permanent phasedPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent destroyedPermanent = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Card drawnCard = new GrizzlyBears();
        Card graveyardCard = new Shock();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        int damageBefore = gd.getLife(player2.getId());

        cast(new int[]{0, 1, 2, 3, 4}, List.of(
                phasedPermanent.getId(),
                player2.getId(),
                player2.getId(),
                player2.getId(),
                destroyedPermanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(phasedPermanent);
        assertThat(gd.playerHands.get(player2.getId())).contains(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(damageBefore - 2);
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    void canChooseTheSameModeMoreThanOnce() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        cast(new int[]{1, 1, 1, 1, 1}, List.of(
                player1.getId(), player1.getId(), player1.getId(), player1.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    void destructionModeOnlyTargetsArtifactsOrEnchantments() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThatThrownBy(() -> cast(new int[]{4, 4, 4, 4, 4}, List.of(
                creature.getId(), creature.getId(), creature.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        cast(new int[]{4, 4, 4, 4, 4}, List.of(
                artifact.getId(), artifact.getId(), artifact.getId(), artifact.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    void graveyardExileOnlyAffectsThePlayerTargetedByThatMode() {
        Card ownGraveyardCard = new Shock();
        Card opposingGraveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opposingGraveyardCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        cast(new int[]{1, 2, 3, 3, 3}, List.of(
                player1.getId(), player2.getId(), player1.getId(), player1.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opposingGraveyardCard);
    }

    @Test
    void repeatedDamageModesKeepTheirSeparateTargets() {
        int ownLife = gd.getLife(player1.getId());
        int opposingLife = gd.getLife(player2.getId());

        cast(new int[]{3, 3, 3, 3, 3}, List.of(
                player1.getId(), player2.getId(), player1.getId(), player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife - 6);
    }

    @Test
    void phasedOutArtifactIsNotDestroyedByALaterMode() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        int opposingLife = gd.getLife(player2.getId());

        cast(new int[]{0, 3, 3, 3, 4}, List.of(
                artifact.getId(), player2.getId(), player2.getId(), player2.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(artifact.getCard());
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife - 6);
    }

    @Test
    void destructionModeCanDestroyAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(new int[]{3, 3, 3, 3, 4}, List.of(
                player2.getId(), player2.getId(), player2.getId(), player2.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enchantment.getCard());
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new UniteTheCoalition()));
        addMana();
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(5, modeIndices),
                null, null, targetIds, List.of());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
