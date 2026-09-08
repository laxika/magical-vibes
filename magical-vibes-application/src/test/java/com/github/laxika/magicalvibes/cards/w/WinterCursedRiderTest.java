package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WinterCursedRiderTest extends BaseCardTest {

    @Test
    void wardProtectsWinterAndCanBePaidWithLife() {
        Permanent winter = addCreatureReady(player1, new WinterCursedRider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player2, 0, winter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveToughness(gd, winter)).isEqualTo(2);
    }

    @Test
    void artifactsYouControlHaveWard() {
        addCreatureReady(player1, new WinterCursedRider());
        Permanent relic = addCreatureReady(player1, new Ornithopter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player2, 0, relic.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void exhaustExilesArtifactCardsAndDebuffsOtherNonartifactCreatures() {
        Permanent winter = addCreatureReady(player1, new WinterCursedRider());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent opposingGiant = addCreatureReady(player2, new HillGiant());
        Ornithopter artifactInGraveyard = new Ornithopter();
        DarksteelRelic relicInGraveyard = new DarksteelRelic();
        Shock nonartifactInGraveyard = new Shock();
        harness.setGraveyard(player1, List.of(artifactInGraveyard, relicInGraveyard, nonartifactInGraveyard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifactInGraveyard.getId(), relicInGraveyard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifactInGraveyard, relicInGraveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonartifactInGraveyard);
        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, winter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, winter)).isEqualTo(2);
    }

    @Test
    void exhaustAbilityCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new WinterCursedRider());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
